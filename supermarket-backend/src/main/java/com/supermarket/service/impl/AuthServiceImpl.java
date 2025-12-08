package com.supermarket.service.impl;

import com.supermarket.dto.LoginRequest;
import com.supermarket.entity.SysRole;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SysRoleMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.AuthService;
import com.supermarket.service.LoginLimitService;
import com.supermarket.service.TokenBlacklistService;
import com.supermarket.utils.JwtUtils;
import com.supermarket.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginLimitService loginLimitService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public LoginVO login(LoginRequest request) {
        System.out.println("========== 登录调试信息 ==========");
        System.out.println("输入用户名: " + request.getUsername());
        System.out.println("输入密码: " + request.getPassword());
        
        // ===== 1. 检查是否被锁定 =====
        if (loginLimitService.isLocked(request.getUsername())) {
            long remainingTime = loginLimitService.getRemainingLockTime(request.getUsername());
            long minutes = remainingTime / 60;
            System.out.println("🔒 账号已被锁定，剩余时间: " + minutes + "分钟");
            throw new BusinessException("登录失败次数过多，账号已被锁定" + minutes + "分钟，请稍后再试");
        }
        
        // 查询用户
        SysUser user = userMapper.selectByUsernameWithRole(request.getUsername());
        if (user == null) {
            System.out.println("❌ 用户不存在");
            // 记录失败次数
            int failCount = loginLimitService.recordLoginFailure(request.getUsername());
            System.out.println("登录失败次数: " + failCount);
            if (failCount >= 5) {
                throw new BusinessException("登录失败次数过多，账号已被锁定30分钟");
            }
            throw new BusinessException("用户名或密码错误，还可尝试" + (5 - failCount) + "次");
        }
        
        System.out.println("✅ 找到用户: " + user.getUsername());
        System.out.println("数据库密码哈希: " + user.getPassword());
        System.out.println("用户状态: " + user.getStatus());

        // 验证密码
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("密码验证结果: " + (matches ? "✅ 匹配成功" : "❌ 匹配失败"));
        
        if (!matches) {
            // ===== 2. 密码错误，记录失败次数 =====
            int failCount = loginLimitService.recordLoginFailure(request.getUsername());
            System.out.println("❌ 密码错误，失败次数: " + failCount);
            System.out.println("====================================");
            
            if (failCount >= 5) {
                throw new BusinessException("登录失败次数过多，账号已被锁定30分钟");
            }
            throw new BusinessException("用户名或密码错误，还可尝试" + (5 - failCount) + "次");
        }
        
        System.out.println("====================================");

        // 验证用户状态
        if (user.getStatus() == 0) {
            System.out.println("❌ 账号已被禁用");
            throw new BusinessException("账号已被禁用");
        }
        System.out.println("✅ 用户状态正常");

        // 查询角色信息
        System.out.println("查询角色信息，roleId: " + user.getRoleId());
        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            System.out.println("❌ 角色不存在，roleId: " + user.getRoleId());
            throw new BusinessException("用户角色不存在");
        }
        System.out.println("✅ 找到角色: " + role.getRoleName() + " (" + role.getRoleCode() + ")");

        // ===== 3. 登录成功，清除失败记录 =====
        loginLimitService.clearLoginFailure(request.getUsername());
        System.out.println("✅ 清除登录失败记录");
        
        // 生成token
        System.out.println("生成 JWT Token...");
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), role.getRoleCode());
        System.out.println("✅ Token 生成成功");
        System.out.println("====================================");

        // 构建返回对象
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())  // 返回用户头像
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .build();
    }

    @Override
    public void logout(String token) {
        // ===== 4. 登出时将 Token 加入黑名单 =====
        try {
            // 移除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 获取 Token 剩余有效时间
            long expireSeconds = jwtUtils.getExpireSeconds(token);
            
            // 加入黑名单
            tokenBlacklistService.addToBlacklist(token, expireSeconds);
            System.out.println("✅ Token 已加入黑名单");
        } catch (Exception e) {
            System.out.println("❌ Token 加入黑名单失败: " + e.getMessage());
        }
    }
}

