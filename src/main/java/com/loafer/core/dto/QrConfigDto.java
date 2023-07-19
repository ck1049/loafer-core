package com.loafer.core.dto;

import cn.hutool.extra.qrcode.QrConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.awt.*;

@Getter
@Setter
@ApiModel("二维码配置")
public class QrConfigDto extends QrConfig {

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

    @Override
    public QrConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public QrConfig setHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public QrConfig setForeColor(int foreColor) {
        this.foreColor = foreColor;
        return this;
    }

    @Override
    public QrConfig setBackColor(int backColor) {
        this.backColor = backColor;
        return this;
    }

    @Override
    public QrConfig setMargin(Integer margin) {
        this.margin = margin;
        return this;
    }

    @Override
    public QrConfig setImg(Image img) {
        this.img = img;
        return this;
    }

    @Override
    public QrConfig setRatio(int ratio) {
        this.ratio = ratio;
        return this;
    }
}
