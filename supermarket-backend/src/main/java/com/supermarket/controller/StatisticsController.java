package com.supermarket.controller;

import com.supermarket.common.Result;
import com.supermarket.service.StatisticsService;
import com.supermarket.vo.StatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/statistics")
@Api(tags = "统计分析")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    @ApiOperation("获取首页统计数据")
    public Result<StatisticsVO> getDashboardStatistics() {
        StatisticsVO statistics = statisticsService.getDashboardStatistics();
        return Result.success(statistics);
    }
}


