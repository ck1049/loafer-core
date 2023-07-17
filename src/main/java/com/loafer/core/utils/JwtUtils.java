package com.loafer.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loafer.core.common.Payload;
import io.jsonwebtoken.*;
import org.joda.time.DateTime;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;

public class JwtUtils {

    private static final String JWT_PAYLOAD_USER_KEY = "user";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 私钥加密token
     *
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位分钟
     * @return JWT
     */
    public static String generateTokenExpireInMinutes(Object userInfo, PrivateKey privateKey, int expire) {
        try {
            DateTime now = DateTime.now();
            return Jwts.builder()
                    .claim(JWT_PAYLOAD_USER_KEY, objectMapper.writeValueAsString(userInfo))
                    .setId(createJTI())
                    .setIssuedAt(now.toDate())
                    .setExpiration(DateTime.now().plusMinutes(expire).toDate())
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 私钥加密token
     *
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位秒
     * @return JWT
     */
    public static String generateTokenExpireInSeconds(Object userInfo, PrivateKey privateKey, int expire) {
        try {
            DateTime now = DateTime.now();
            return Jwts.builder()
                    .claim(JWT_PAYLOAD_USER_KEY, objectMapper.writeValueAsString(userInfo))
                    .setId(createJTI())
                    .setIssuedAt(now.toDate())
                    .setExpiration(now.plusSeconds(expire).toDate())
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥解析token
     *
     * @param token     用户请求中的token
     * @param publicKey 公钥
     * @return Jws<Claims>
     */
    private static Jws<Claims> parserToken(String token, PublicKey publicKey) throws ExpiredJwtException {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
    }

    private static String createJTI() {
        return new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
    }

    /**
     * 获取token中的用户信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static <T> Payload<T> getInfoFromToken(String token, PublicKey publicKey, Class<T> userType) throws ExpiredJwtException {

        try {
            Jws<Claims> claimsJws = parserToken(token, publicKey);
            Claims body = claimsJws.getBody();
            Payload<T> claims = new Payload<>();
            claims.setId(body.getId());
            claims.setUserInfo(objectMapper.readValue(body.get(JWT_PAYLOAD_USER_KEY).toString(), userType));

            claims.setCreateTime(body.getIssuedAt());
            claims.setExpiration(body.getExpiration());
            return claims;
        } catch (ExpiredJwtException e) {
            // 抛出jwt过期异常，refreshToken时使用
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            //取消抛出异常，否则获取不到签发时间、过期时间
            //throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取token中的载荷信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static <T> Payload<T> getInfoFromToken(String token, PublicKey publicKey) throws ExpiredJwtException {
        Payload<T> claims = null;
        try {
            Jws<Claims> claimsJws = parserToken(token, publicKey);
            Claims body = claimsJws.getBody();
            claims = new Payload<>();
            claims.setId(body.getId());
            claims.setCreateTime(body.getIssuedAt());
            claims.setExpiration(body.getExpiration());
        } catch (ExpiredJwtException e) {
            // 抛出jwt过期异常，refreshToken时使用
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), e.getMessage());
        }
        return claims;
    }
}
