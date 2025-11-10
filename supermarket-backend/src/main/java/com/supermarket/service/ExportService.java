package com.supermarket.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 数据导出服务接口
 */
public interface ExportService {
    
    /**
     * 导出商品数据到Excel
     */
    void exportProducts(HttpServletResponse response) throws IOException;
}


