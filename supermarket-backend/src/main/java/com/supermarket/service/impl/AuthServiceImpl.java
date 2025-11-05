package com.supermarket.service.impl;

import com.supermarket.dto.LoginRequest;
import com.supermarket.entity.SysRole;
import com.supermarket.entity.SysUser;
import com.supermarket.exception.BusinessException;
import com.supermarket.mapper.SysRoleMapper;
import com.supermarket.mapper.SysUserMapper;
import com.supermarket.service.AuthService;
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

    @Override
    public LoginVO login(LoginRequest request) {
        System.out.println("========== 登录调试信息 ==========");
        System.out.println("输入用户名: " + request.getUsername());
        System.out.println("输入密码: " + request.getPassword());
        
        // 查询用户
        SysUser user = userMapper.selectByUsernameWithRole(request.getUsername());
        if (user == null) {
            System.out.println("❌ 用户不存在");
            throw new BusinessException("用户名或密码错误");
        }
        
        System.out.println("✅ 找到用户: " + user.getUsername());
        System.out.println("数据库密码哈希: " + user.getPassword());
        System.out.println("用户状态: " + user.getStatus());

        // 验证密码
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("密码验证结果: " + (matches ? "✅ 匹配成功" : "❌ 匹配失败"));
        System.out.println("====================================");
        
        if (!matches) {
            throw new BusinessException("用户名或密码错误");
        }

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
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .build();
    }

    @Override
    public void logout(String token) {
        // 这里可以将token加入黑名单（使用Redis实现）
        // 目前简单处理，客户端清除token即可
    }
}

