package com.lsj.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lsj.common.constant.CommonConstants;
import com.lsj.common.enums.ResultCode;
import com.lsj.common.exception.BusinessException;
import com.lsj.common.result.Result;
import com.lsj.user.domain.dto.LoginDTO;
import com.lsj.user.domain.dto.RegisterDTO;
import com.lsj.user.domain.po.User;
import com.lsj.user.domain.vo.LoginVO;
import com.lsj.user.domain.vo.UserVO;
import com.lsj.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据ID查询用户（返回脱敏信息）
     */
    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return Result.success(userService.toVO(user));
    }

    /**
     * 根据用户名查询（返回脱敏信息）
     */
    @GetMapping("/username/{username}")
    public Result<UserVO> getByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return Result.success(userService.toVO(user));
    }

    /**
     * 分页查询用户列表（返回脱敏信息）
     */
    @GetMapping("/page")
    public Result<Page<UserVO>> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
        Page<User> userPage = userService.page(new Page<>(pageNum, pageSize));
        // PO 转 VO
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream()
                .map(userService::toVO)
                .collect(java.util.stream.Collectors.toList()));
        return Result.success(voPage);
    }

    /**
     * 新增用户（管理端，密码自动加密）
     */
    @PostMapping
    public Result<Void> add(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能为空");
        }
        // BCrypt 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userService.save(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Void> update(@RequestBody User user) {
        if (user.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        // 不允许通过此接口修改密码，密码修改走专门接口
        user.setPassword(null);
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 根据ID删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }
}
