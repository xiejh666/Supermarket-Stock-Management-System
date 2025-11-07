package com.supermarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.mapper.CategoryMapper;
import com.supermarket.mapper.ProductMapper;
import com.supermarket.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据导出服务实现
 */
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {
    
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    
    @Override
    public void exportProducts(HttpServletResponse response) throws IOException {
        // 查询所有商品
        List<Product> products = productMapper.selectList(null);
        
        // 查询所有分类（用于显示分类名称）
        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getCategoryName());
        }
        
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品信息");
        
        // 创建标题行样式
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        // 创建数据行样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"商品编号", "商品名称", "商品分类", "单位", "成本价", "售价", "商品描述", "状态", "创建时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            
            // 设置列宽
            if (i == 6) { // 商品描述列加宽
                sheet.setColumnWidth(i, 8000);
            } else {
                sheet.setColumnWidth(i, 4000);
            }
        }
        
        // 填充数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            
            // 商品编号
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(product.getProductCode() != null ? product.getProductCode() : "");
            cell0.setCellStyle(dataStyle);
            
            // 商品名称
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(product.getProductName() != null ? product.getProductName() : "");
            cell1.setCellStyle(dataStyle);
            
            // 商品分类
            Cell cell2 = row.createCell(2);
            String categoryName = categoryMap.get(product.getCategoryId());
            cell2.setCellValue(categoryName != null ? categoryName : "未分类");
            cell2.setCellStyle(dataStyle);
            
            // 单位
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(product.getUnit() != null ? product.getUnit() : "");
            cell3.setCellStyle(dataStyle);
            
            // 成本价
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(product.getCostPrice() != null ? product.getCostPrice().doubleValue() : 0.0);
            cell4.setCellStyle(dataStyle);
            
            // 售价
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
            cell5.setCellStyle(dataStyle);
            
            // 商品描述
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(product.getDescription() != null ? product.getDescription() : "");
            cell6.setCellStyle(dataStyle);
            
            // 状态
            Cell cell7 = row.createCell(7);
            String statusText = product.getStatus() != null && product.getStatus() == 1 ? "上架" : "下架";
            cell7.setCellValue(statusText);
            cell7.setCellStyle(dataStyle);
            
            // 创建时间
            Cell cell8 = row.createCell(8);
            cell8.setCellValue(product.getCreateTime() != null ? product.getCreateTime().format(formatter) : "");
            cell8.setCellStyle(dataStyle);
        }
        
        // 设置响应头
        String fileName = "商品信息_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        
        // 写入响应输出流
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}

