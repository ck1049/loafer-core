package com.loafer.core.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author loafer
 * @since 2023-07-16
 */
@Getter
@Setter
@ApiModel(value = "Users对象", description = "")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("电子邮件")
    private String email;

    @ApiModelProperty("名字")
    private String firstName;

    @ApiModelProperty("姓氏")
    private String lastName;

    @ApiModelProperty("电话号码")
    private String phone;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("州/省份")
    private String state;

    @ApiModelProperty("国家")
    private String country;

    @ApiModelProperty("邮政编码")
    private String postalCode;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("是否删除 0：是；1：否")
    private Integer isDelete;


}
