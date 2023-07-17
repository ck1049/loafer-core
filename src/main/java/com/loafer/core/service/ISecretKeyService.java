package com.loafer.core.service;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * jwt/rsa秘钥管理服务
 */
public interface ISecretKeyService {

    /**
     * 获取公钥
     * @return
     */
    PublicKey getPublicKey();

    /**
     * 获取私钥
     * @return
     */
    PrivateKey getPrivateKey();
}
