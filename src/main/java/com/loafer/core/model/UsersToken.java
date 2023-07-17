package com.loafer.core.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@TableName("users_token")
@ApiModel(value = "UsersToken对象", description = "")
public class UsersToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("短期tokenId")
    private String tokenId;

    @ApiModelProperty("长期token")
    private String token;

    @ApiModelProperty("有效期")
    private Long expire;

    @ApiModelProperty("有效期单位")
    private String expireUnit;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("过期时间")
    private Date expireTime;

    @ApiModelProperty("使用次数")
    private Integer used;

    public UsersToken(String tokenId) {
        this.tokenId = tokenId;
    }

    public UsersToken(String tokenId, int used) {
        this.tokenId = tokenId;
        this.used = used;
    }

    public UsersToken(String tokenId, Date expireTime) {
        this.tokenId = tokenId;
        this.expireTime = expireTime;
    }

}
