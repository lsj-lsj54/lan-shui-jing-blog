package com.lsj.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析与校验
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 密钥（生产环境应从配置中心读取，且长度至少 256 位）
     */
    @Value("${jwt.secret:lsj-blog-secret-key-2024-must-be-at-least-256-bits-long}")
    private String secret;

    /**
     * Token 过期时间，默认 24 小时（毫秒）
     */
    @Value("${jwt.expire:86400000}")
    private Long expire;

    /**
     * 生成 Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims);
    }

    /**
     * 从 Token 中解析用户ID
     *
     * @param token JWT Token
     * @return 用户ID，解析失败返回 null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 从 Token 中解析用户名
     *
     * @param token JWT Token
     * @return 用户名，解析失败返回 null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object username = claims.get("username");
        return username == null ? null : username.toString();
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token JWT Token
     * @return true-有效，false-无效或已过期
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.after(new Date());
    }

    /**
     * 内部方法：创建 Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 内部方法：解析 Token
     */
    private Claims parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 内部方法：构建签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
