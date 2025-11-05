package com.supermarket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.common.Result;
import com.supermarket.dto.UserDTO;
import com.supermarket.service.SysUserService;
import com.supermarket.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@Api(tags = "用户管理")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/list")
    @ApiOperation("分页查询用户列表")
    public Result<Page<UserVO>> getUserList(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("用户名") @RequestParam(required = false) String username,
            @ApiParam("真实姓名") @RequestParam(required = false) String realName,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.getUserList(current, size, username, realName, status);
        return Result.success(page);
    }

    @PostMapping
    @ApiOperation("创建用户")
    public Result<Void> createUser(@Valid @RequestBody UserDTO dto) {
        userService.createUser(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("更新用户")
    public Result<Void> updateUser(@Valid @RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @ApiOperation("修改用户状态")
    public Result<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    @ApiOperation("重置用户密码")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
}


