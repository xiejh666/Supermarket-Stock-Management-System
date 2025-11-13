package com.supermarket.service;

import com.supermarket.vo.StatisticsVO;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取首页统计数据
     * @param period 时间周期 (week/month/year)
     * @return 统计数据
     */
    StatisticsVO getDashboardStatistics(String period);
}
