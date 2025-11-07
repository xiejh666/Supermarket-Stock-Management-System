package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.SystemSettingsDTO;
import com.supermarket.service.ExportService;
import com.supermarket.service.SystemSettingsService;
import com.supermarket.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/settings")
@Api(tags = "系统设置管理")
@RequiredArgsConstructor
public class SystemSettingsController {
    
    private final SystemSettingsService systemSettingsService;
    private final ExportService exportService;
    private final JwtUtils jwtUtils;
    
    /**
     * 获取系统设置
     */
    @GetMapping
    @ApiOperation("获取系统设置")
    public Result<SystemSettingsDTO> getSettings(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        SystemSettingsDTO settings = systemSettingsService.getSettings(userId);
        return Result.success(settings);
    }
    
    /**
     * 保存系统设置
     */
    @PostMapping
    @ApiOperation("保存系统设置")
    public Result<Void> saveSettings(@RequestBody SystemSettingsDTO settings, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        systemSettingsService.saveSettings(userId, settings);
        return Result.success();
    }
    
    /**
     * 导出商品数据
     */
    @GetMapping("/export/products")
    @ApiOperation("导出商品数据")
    public void exportProducts(HttpServletResponse response) throws IOException {
        exportService.exportProducts(response);
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token != null) {
            return jwtUtils.getUserIdFromToken(token);
        }
        return null;
    }
    
    /**
     * 从请求头中提取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

