package com.lsj.user.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import com.lsj.common.util.JwtUtil;
import com.lsj.user.domain.dto.LoginDTO;
import com.lsj.user.domain.dto.RegisterDTO;
import com.lsj.user.domain.po.User;
import com.lsj.user.domain.vo.LoginVO;
import com.lsj.user.domain.vo.UserVO;
import com.lsj.user.mapper.UserMapper;
import com.lsj.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 根据用户名查询
        User user = getByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // BCrypt 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // 校验状态
        if (user.getStatus() != null && user.getStatus() == CommonConstants.USER_STATUS_DISABLED) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号已被禁用");
        }
        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(toVO(user));
        return loginVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        User existUser = getByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        // 构建用户实体，BCrypt 加密密码
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(CommonConstants.USER_STATUS_NORMAL);
        save(user);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return toVO(user);
    }

    @Override
    public User getByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }

    @Override
    public UserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
