package com.supermarket.controller;

import com.supermarket.service.BusinessOperationNotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 测试通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
@RequiredArgsConstructor
public class TestNotificationController {

    private final BusinessOperationNotificationService businessNotificationService;

    @PostMapping("/notification")
    @ApiOperation("测试发送通知")
    public String testNotification(@RequestParam Long operatorId, 
                                 @RequestParam String operatorRole) {
        try {
            businessNotificationService.notifyPurchaseOperation(
                operatorId, operatorRole, "CREATE", "TEST123", 999L, 100.0
            );
            
            return "通知发送成功！请检查管理员是否收到通知";
        } catch (Exception e) {
            log.error("测试通知发送失败", e);
            return "通知发送失败：" + e.getMessage();
        }
    }

    @PostMapping("/product-notification")
    @ApiOperation("测试商品通知")
    public String testProductNotification(@RequestParam Long operatorId, 
                                        @RequestParam String operatorRole) {
        try {
            businessNotificationService.notifyProductOperation(
                operatorId, operatorRole, "CREATE", "测试商品", "TEST001", 999L
            );
            
            return "商品通知发送成功！请检查是否收到通知";
        } catch (Exception e) {
            log.error("测试商品通知发送失败", e);
            return "商品通知发送失败：" + e.getMessage();
        }
    }
}
