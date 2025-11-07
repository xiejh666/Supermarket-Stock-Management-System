package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.MessageNotificationService;
import com.supermarket.utils.JwtUtils;
import com.supermarket.vo.MessageNotificationVO;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/messages")
@Api(tags = "消息通知管理")
@RequiredArgsConstructor
public class MessageNotificationController {
    
    private final MessageNotificationService messageService;
    private final JwtUtils jwtUtils;
    
    /**
     * 从请求中获取用户信息
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
     * 从请求中获取角色编码
     */
    private String getRoleCodeFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            Claims claims = jwtUtils.getClaimsFromToken(token);
            return claims.get("roleCode", String.class);
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
     * 获取当前用户的消息列表
     */
    @GetMapping("/list")
    @ApiOperation("获取消息列表")
    public Result<List<MessageNotificationVO>> getMessageList(
            @ApiParam("是否只查询未读") @RequestParam(required = false, defaultValue = "false") Boolean onlyUnread,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        String roleCode = getRoleCodeFromRequest(request);
        
        if (userId == null || roleCode == null) {
            return Result.error("无法获取用户信息");
        }
        
        List<MessageNotificationVO> messages = messageService.getUserMessages(userId, roleCode, onlyUnread);
        return Result.success(messages);
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    @ApiOperation("获取未读消息数量")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        String roleCode = getRoleCodeFromRequest(request);
        
        if (userId == null || roleCode == null) {
            return Result.error("无法获取用户信息");
        }
        
        Integer count = messageService.getUnreadCount(userId, roleCode);
        return Result.success(count);
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    @ApiOperation("标记消息为已读")
    public Result<Void> markAsRead(@ApiParam("消息ID") @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        boolean success = messageService.markAsRead(id, userId);
        return success ? Result.success() : Result.error("操作失败");
    }
    
    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    @ApiOperation("标记所有消息为已读")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        String roleCode = getRoleCodeFromRequest(request);
        
        if (userId == null || roleCode == null) {
            return Result.error("无法获取用户信息");
        }
        
        boolean success = messageService.markAllAsRead(userId, roleCode);
        return success ? Result.success() : Result.error("操作失败");
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除消息")
    public Result<Void> deleteMessage(@ApiParam("消息ID") @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        boolean success = messageService.deleteMessage(id, userId);
        return success ? Result.success() : Result.error("操作失败");
    }
    
    /**
     * 清空所有消息
     */
    @DeleteMapping("/clear-all")
    @ApiOperation("清空所有消息")
    public Result<Void> clearAllMessages(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        String roleCode = getRoleCodeFromRequest(request);
        
        if (userId == null || roleCode == null) {
            return Result.error("无法获取用户信息");
        }
        
        boolean success = messageService.clearAllMessages(userId, roleCode);
        return success ? Result.success() : Result.error("操作失败");
    }
}

