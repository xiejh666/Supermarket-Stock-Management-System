package com.supermarket.filter;

import com.supermarket.service.TokenBlacklistService;
import com.supermarket.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 获取 Token
        String token = getTokenFromRequest(request);
        
        // 验证 Token
        if (StringUtils.hasText(token)) {
            // ===== 检查 Token 是否在黑名单中 =====
            if (tokenBlacklistService.isBlacklisted(token)) {
                System.out.println("🚫 Token 在黑名单中，拒绝访问");
                // Token 已失效，不设置认证信息，继续过滤链（会被拦截）
                filterChain.doFilter(request, response);
                return;
            }
            
            // 验证 Token 有效性
            if (jwtUtils.validateToken(token)) {
                // 解析 Token
                Claims claims = jwtUtils.getClaimsFromToken(token);
                String username = claims.get("username", String.class);
                String roleCode = claims.get("roleCode", String.class);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleCode))
                    );
                
                // 设置到 Spring Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        // 继续过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从 Header 中获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

