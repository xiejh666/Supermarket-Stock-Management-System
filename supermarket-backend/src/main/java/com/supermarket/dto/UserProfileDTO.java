package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户个人资料DTO
 */
@Data
@ApiModel("用户个人资料DTO")
public class UserProfileDTO {
    
    @ApiModelProperty("用户ID")
    private Long id;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("真实姓名")
    private String realName;
    
    @ApiModelProperty("手机号")
    private String phone;
    
    @ApiModelProperty("邮箱")
    private String email;
    
    @ApiModelProperty("角色名称")
    private String roleName;
    
    @ApiModelProperty("角色编码")
    private String roleCode;
    
    @ApiModelProperty("账号状态")
    private Integer status;
}

