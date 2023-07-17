package com.loafer.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Rsa工具类
 */
public class RsaUtils {

    private static final int DEFAULT_KEY_SIZE = 2048;

    /**
     * 从文件中读取公钥
     * @param filename 公钥保存路径，相对于classpath
     * @return  PublicKey 公钥对象
     * @throws Exception
     */
    public static PublicKey getPublicKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPublicKey(bytes);
    }

    /**
     * 从文件中读取密钥
     * @param filename 私钥保存路径，相对于classpath
     * @return  PrivateKey 私钥对象
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPrivateKey(bytes);
    }

    /**
     * 获取公钥
     * 公钥的字节形式。
     * @param bytes 公钥的字节形式
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(byte[] bytes) throws Exception {
        bytes = Base64.getDecoder().decode(bytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * 获取密钥
     * @param bytes 私钥的字节形式
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        bytes = Base64.getDecoder().decode(bytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * 根据密文，生成rsa公钥和私钥,并写入指定文件
     * @param publicKeyFilename  公钥文件路径
     * @param privateKeyFilename 私钥文件路径
     * @param secret             生成密钥的密文
     */
    public static void generateKey(String publicKeyFilename, String privateKeyFilename, String secret, int keySize) throws Exception {
        // 获取公钥并写出
        writeFile(publicKeyFilename, generatePublicKey(secret, keySize));
        // 获取私钥并写出
        writeFile(privateKeyFilename, generatePrivateKey(secret, keySize));
    }

    /**
     * 生成公钥的base64字节数组
     * @param secret
     * @param keySize
     * @return
     * @throws Exception
     */
    public static byte[] generatePublicKey(String secret, int keySize) throws Exception {
        // 获取公钥并写出
        byte[] publicKeyBytes = getKeyPair(secret, keySize).getPublic().getEncoded();
        return Base64.getEncoder().encode(publicKeyBytes);
    }

    /**
     * 生成私钥的base64字节数组
     * @param secret
     * @param keySize
     * @return
     * @throws Exception
     */
    public static byte[] generatePrivateKey(String secret, int keySize) throws Exception {
        // 获取私钥并写出
        byte[] privateKeyBytes = getKeyPair(secret, keySize).getPrivate().getEncoded();
        return Base64.getEncoder().encode(privateKeyBytes);
    }

    private static KeyPair getKeyPair(String secret, int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(Math.max(keySize, DEFAULT_KEY_SIZE), secureRandom);
        return keyPairGenerator.genKeyPair();
    }

    private static byte[] readFile(String fileName) throws Exception {
        return Files.readAllBytes(new File(fileName).toPath());
    }

    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        if(!dest.exists()) {
            dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }

}
