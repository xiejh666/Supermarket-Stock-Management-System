package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.dto.ChangePasswordRequest;
import com.supermarket.dto.UpdateProfileRequest;
import com.supermarket.dto.UserProfileDTO;
import com.supermarket.entity.SysUser;
import com.supermarket.mapper.SysRoleMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.SysUserService;
import com.supermarket.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户个人资料控制器
 */
@RestController
@RequestMapping("/user/profile")
@Api(tags = "用户个人资料管理")
@RequiredArgsConstructor
public class UserProfileController {
    
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserService userService;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            Claims claims = jwtUtils.getClaimsFromToken(token);
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                return Long.valueOf(userIdObj.toString());
            }
        }
        return null;
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping
    @ApiOperation("获取当前用户信息")
    public Result<UserProfileDTO> getUserProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        // 查询用户信息（包含角色信息）
        SysUser user = userMapper.selectUserWithRole(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 转换为DTO
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());  // 返回头像URL
        dto.setStatus(user.getStatus());
        dto.setRoleName(user.getRoleName());
        dto.setRoleCode(user.getRoleCode());
        
        return Result.success(dto);
    }
    
    /**
     * 更新个人资料
     */
    @PutMapping
    @ApiOperation("更新个人资料")
    public Result<Void> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        // 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 更新信息
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        
        int result = userMapper.updateById(user);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("更新失败");
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }
        
        // 查询用户
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        
        int result = userMapper.updateById(user);
        
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("修改密码失败");
        }
    }
    
    /**
     * 更新用户头像
     */
    @PutMapping("/avatar")
    @ApiOperation("更新用户头像")
    public Result<Void> updateAvatar(
            @RequestParam("avatarUrl") String avatarUrl,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        if (!StringUtils.hasText(avatarUrl)) {
            return Result.error("头像URL不能为空");
        }
        
        try {
            userService.updateAvatar(userId, avatarUrl);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新头像失败：" + e.getMessage());
        }
    }
}


