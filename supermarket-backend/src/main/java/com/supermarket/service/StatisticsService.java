package com.supermarket.service;

import com.supermarket.vo.StatisticsVO;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取首页统计数据
     */
    StatisticsVO getDashboardStatistics();
}



