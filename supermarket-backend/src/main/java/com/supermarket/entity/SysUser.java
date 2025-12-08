package com.supermarket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;  // 用户头像URL
    private Integer status;
    private Long roleId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 角色名称（非数据库字段，通过JOIN查询获取）
     */
    @TableField(exist = false)
    private String roleName;
    
    /**
     * 角色编码（非数据库字段，通过JOIN查询获取）
     */
    @TableField(exist = false)
    private String roleCode;
}


