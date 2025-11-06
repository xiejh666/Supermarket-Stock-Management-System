package com.supermarket.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
@ApiModel("登录请求")
public class LoginRequest {

    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}




