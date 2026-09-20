package com.lsj.user.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lsj.user.domain.dto.LoginDTO;
import com.lsj.user.domain.dto.RegisterDTO;
import com.lsj.user.domain.po.User;
import com.lsj.user.domain.vo.LoginVO;
import com.lsj.user.domain.vo.UserVO;

public interface IUserService extends IService<User> {

    /**
     * 登录，返回 Token 和脱敏用户信息
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO dto);

    /**
     * 根据用户名查询
     */
    User getByUsername(String username);

    /**
     * PO 转 VO（脱敏）
     */
    UserVO toVO(User user);
}
