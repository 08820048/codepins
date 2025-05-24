package cn.ilikexff.codepins.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 字符串加密工具类
 *
 * @deprecated 此类已废弃。插件现在完全免费开源，不再需要加密功能。
 *             保留此类仅用于向后兼容，解密可能存在的旧数据。
 */
@Deprecated
public class StringEncryptor {

    // 注意：此类已废弃，插件现在完全免费开源，不再需要加密功能
    // 保留此密钥仅用于向后兼容，解密旧数据
    private static final String SECRET_KEY = "DEPRECATED_KEY_16";

    // 加密算法
    private static final String ALGORITHM = "AES";

    // 加密模式和填充方式
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @return 密文（Base64编码）
     */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // 出错时返回原文，确保功能不受影响
            System.err.println("加密失败: " + e.getMessage());
            return plainText;
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedText 密文（Base64编码）
     * @return 明文
     */
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 出错时返回原文，确保功能不受影响
            System.err.println("解密失败: " + e.getMessage());
            return encryptedText;
        }
    }

    /**
     * 混淆字符串（简单的字符替换）
     * 这是一种简单的混淆方法，不是真正的加密
     *
     * @param input 输入字符串
     * @return 混淆后的字符串
     */
    public static String obfuscate(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            // 简单的字符替换规则
            result.append((char) (c + 5));
        }
        return result.toString();
    }

    /**
     * 解混淆字符串
     *
     * @param input 混淆后的字符串
     * @return 原始字符串
     */
    public static String deobfuscate(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            // 反向字符替换
            result.append((char) (c - 5));
        }
        return result.toString();
    }
}
