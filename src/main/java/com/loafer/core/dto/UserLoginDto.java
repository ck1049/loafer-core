package com.loafer.core.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("用户登录请求类")
public class UserLoginDto {

    @NotEmpty(message = "用户名不能为空！")
    private String userName;

    @NotEmpty(message = "密码不能为空！")
    private String password;
}
