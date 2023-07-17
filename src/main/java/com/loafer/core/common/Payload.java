package com.loafer.core.common;

import lombok.Data;
import java.util.Date;

@Data
public class Payload<T> {

    // jwt的id
    private String id;

    // 用户信息
    private T userInfo;

    // 创建时间
    private Date createTime;

    // 过期时间
    private Date expiration;

}
