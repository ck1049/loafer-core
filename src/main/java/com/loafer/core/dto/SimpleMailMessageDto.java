package com.loafer.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("邮件发送请求类")
public class SimpleMailMessageDto {

    @NotEmpty(message = "收件人地址不能为空!")
    @ApiModelProperty("收件人地址")
    private String[] to;
    @NotBlank(message = "邮件主题不能为空!")
    @ApiModelProperty("邮件主题")
    private String subject;
    @ApiModelProperty("邮件内容")
    private String text;
}
