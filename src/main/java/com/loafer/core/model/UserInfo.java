package com.loafer.core.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 载荷 ：UserInfo
 */
@Data
@ApiModel("用户信息")
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("角色")
    private String role;
}

