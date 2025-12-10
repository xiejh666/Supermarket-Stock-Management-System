package com.supermarket.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 验证码VO
 */
@Data
@Builder
@ApiModel("验证码信息")
public class CaptchaVO {

    @ApiModelProperty(value = "验证码UUID")
    private String uuid;

    @ApiModelProperty(value = "验证码Base64图片")
    private String img;
}
