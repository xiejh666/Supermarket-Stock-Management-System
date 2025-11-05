package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.entity.SysRole;
import com.supermarket.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@Api(tags = "角色管理")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/list")
    @ApiOperation("查询所有角色")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleService.getAllRoles();
        return Result.success(roles);
    }
}


