package com.supermarket;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密测试工具
 * 用于生成和验证 BCrypt 密码
 */
public class PasswordTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 原始密码
        String rawPassword = "123456";
        
        // 生成加密密码
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("========================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("========================================");
        
        // 验证数据库中的密码
        String dbPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDPW";
        boolean matches = encoder.matches(rawPassword, dbPassword);
        System.out.println("\n验证数据库中的密码:");
        System.out.println("数据库密码: " + dbPassword);
        System.out.println("验证结果: " + (matches ? "✅ 匹配成功" : "❌ 匹配失败"));
        System.out.println("========================================");
        
        // 生成新的密码（每次都不同）
        System.out.println("\n新生成的密码哈希（可用于更新数据库）:");
        for (int i = 0; i < 3; i++) {
            String newHash = encoder.encode(rawPassword);
            System.out.println((i+1) + ". " + newHash);
            System.out.println("   验证: " + (encoder.matches(rawPassword, newHash) ? "✅" : "❌"));
        }
        System.out.println("========================================");
    }
}


