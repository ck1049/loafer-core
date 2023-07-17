package com.loafer.core.service.impl;

import com.loafer.core.service.ISecretKeyService;
import com.loafer.core.utils.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Service
public class SecretKeyServiceImpl implements ISecretKeyService {

    private final String SECRET = "LOAFER-CORE-SECRET";
    private final int KEY_SIZE = 2048;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Override
    public PublicKey getPublicKey() {
        try {
            String publicKeyString = redisTemplate.opsForValue().get("LOAFER-CORE-PUBLIC-KEY");
            if (StringUtils.isBlank(publicKeyString)) {
                // redis中未查询到公钥，先生成公钥，再存入redis
                byte[] bytes = RsaUtils.generatePublicKey(SECRET, KEY_SIZE);
                publicKeyString = new String(bytes);
                redisTemplate.opsForValue().set("LOAFER-CORE-PUBLIC-KEY", publicKeyString);
            }
            return RsaUtils.getPublicKey(publicKeyString.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        try {
            String privateKeyString = redisTemplate.opsForValue().get("LOAFER-CORE-PRIVATE-KEY");
            if (StringUtils.isBlank(privateKeyString)) {
                // redis中未查询到私钥，先生成私钥，再存入redis
                byte[] bytes = RsaUtils.generatePrivateKey(SECRET, KEY_SIZE);
                privateKeyString = new String(bytes);
                redisTemplate.opsForValue().set("LOAFER-CORE-PRIVATE-KEY", privateKeyString);
            }
            return RsaUtils.getPrivateKey(privateKeyString.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
