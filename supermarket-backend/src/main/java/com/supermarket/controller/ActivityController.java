package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.ActivityService;
import com.supermarket.vo.ActivityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 最新动态控制器
 */
@RestController
@RequestMapping("/activities")
@Api(tags = "最新动态")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/recent")
    @ApiOperation("获取最新动态")
    public Result<List<ActivityVO>> getRecentActivities(
            @ApiParam("当前用户ID") @RequestParam Long userId,
            @ApiParam("类型筛选：all-全部，purchase-采购，sale-销售，inventory-库存") 
            @RequestParam(defaultValue = "all") String type,
            @ApiParam("数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        List<ActivityVO> activities = activityService.getRecentActivities(userId, type, limit);
        return Result.success(activities);
    }
}
