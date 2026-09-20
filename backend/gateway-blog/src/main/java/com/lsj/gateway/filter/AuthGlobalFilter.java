package com.lsj.gateway.filter;

import com.lsj.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 网关全局鉴权过滤器
 * 1. 白名单路径直接放行
 * 2. 其他路径校验 JWT Token，解析后将 user-id、username 注入请求头传递给下游服务
 * 3. Token 无效或过期返回 401
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 白名单路径（不需要登录即可访问）
     * 注意：这里匹配的是网关原始路径，即包含 /api 前缀
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 用户登录注册
            "/api/user/login",
            "/api/user/register",
            // 文章公开浏览
            "/api/article/page",
            "/api/article/*/view",  // 预留
            // 文章详情（GET）
            "/api/article/*",
            // 评论列表
            "/api/article/comment/list/*",
            // 分类列表（公开浏览，未登录返回空）
            "/api/article/category/list"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String token = getTokenFromRequest(request);

        // 1. 白名单路径：如果有有效 token 就解析并注入 user-id（用于点赞状态等），没有 token 也放行
        if (isWhiteList(request)) {
            if (token != null && !token.isEmpty() && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                if (userId != null) {
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("user-id", String.valueOf(userId))
                            .header("username", username != null ? username : "")
                            .build();
                    log.debug("白名单路径携带Token, userId={}, path={}", userId, path);
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }
            }
            log.debug("白名单路径放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 非白名单路径：必须有有效 Token
        if (token == null || token.isEmpty()) {
            log.warn("无 Token，拒绝访问: {}", path);
            return unauthorizedResponse(exchange, "未登录或Token已过期");
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 无效或已过期，拒绝访问: {}", path);
            return unauthorizedResponse(exchange, "Token无效或已过期");
        }

        // 3. 解析用户信息，注入请求头传递给下游
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        if (userId == null) {
            return unauthorizedResponse(exchange, "Token解析失败");
        }

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("user-id", String.valueOf(userId))
                .header("username", username != null ? username : "")
                .build();

        log.debug("鉴权通过, userId={}, path={}", userId, path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        // 优先级：数值越小越先执行
        return -100;
    }

    /**
     * 判断是否为白名单路径
     */
    private boolean isWhiteList(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "";

        // 通用白名单（所有方法）
        for (String pattern : WHITE_LIST) {
            if (PATH_MATCHER.match(pattern, path)) {
                // 文章详情特殊处理：只放行 GET 请求，且 ID 为数字
                if ("/api/article/*".equals(pattern)) {
                    if (!"GET".equalsIgnoreCase(method)) {
                        return false;
                    }
                    // 提取文章ID，校验是否为数字（避免误放行 /api/article/like 等）
                    String articleId = path.substring("/api/article/".length());
                    return articleId.matches("\\d+");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求中提取 Token
     * 支持 Authorization: Bearer xxx 和 token 参数两种方式
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        // 优先从 Authorization 头获取
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 兼容直接传 token 头
        String token = request.getHeaders().getFirst("token");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return null;
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":401,\"msg\":\"%s\",\"data\":null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
