package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.LoginRequest;
import com.supermarket.service.AuthService;
import com.supermarket.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.supermarket.vo.CaptchaVO;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Api(tags = "认证管理")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    @ApiOperation("获取验证码")
    public Result<CaptchaVO> getCaptcha() {
        return Result.success(authService.getCaptcha());
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = authService.login(request);
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    @ApiOperation("用户登出")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }
}


