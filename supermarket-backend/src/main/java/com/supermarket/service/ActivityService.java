package com.supermarket.service;

import com.supermarket.vo.ActivityVO;

import java.util.List;

/**
 * 最新动态服务接口
 */
public interface ActivityService {
    
    /**
     * 获取最新动态
     * @param userId 当前用户ID
     * @param type 类型筛选：all-全部，purchase-采购，sale-销售，inventory-库存
     * @param limit 数量限制
     * @return 最新动态列表
     */
    List<ActivityVO> getRecentActivities(Long userId, String type, Integer limit);
}
