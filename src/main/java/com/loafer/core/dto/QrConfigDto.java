package com.loafer.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel("二维码配置")
public class QrConfigDto {

    @NotBlank(message = "二维码内容不能为空")
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("图片宽度")
    private int width = 400;
    @ApiModelProperty("图片高度")
    private int height = 400;
    @ApiModelProperty("前景色")
    private int foreColor = 0xFF000000;
    @ApiModelProperty("背景色")
    private int backColor = 0xFFFFFFFF;
    /** 边距1~4 **/
    @ApiModelProperty("边距")
    private Integer margin = 2;
    private int ratio = 6;

}
