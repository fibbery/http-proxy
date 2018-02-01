package com.fibbery.utils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * rsa加密工具类
 * @author fibbery
 * @date 18/1/19
 */
public class RsaUtils {

    public static final String KEY_ALGORITHM = "rsa";

    public static final String SIGN_ALGORITHM = "MD5withRSA";

    public static KeyPair generateRsaKeyPair() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            generator.initialize(2048); //一般设置512或者1024
            keyPair = generator.generateKeyPair();
            return keyPair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    /**
     * 获取base64编码后的私钥
     * @param pair
     * @return
     */
    public static String getPrivateKey(KeyPair pair) {
        return base64Encode(pair.getPrivate().getEncoded());
    }

    /**
     * 获取base64编码后的公钥
     * @param pair
     * @return
     */
    public static String getPublicKey(KeyPair pair) {
        return base64Encode(pair.getPublic().getEncoded());
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    /**
     * 获取公钥实体
     * @param publicKey
     * @return
     */
    public static RSAPublicKey getPublicKey(String publicKey) {
        byte[] keyBytes = base64Decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPublicKey key = (RSAPublicKey) factory.generatePublic(spec);
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RSAPrivateKey getPrivateKey(String privateKey) {
        byte[] keyBytes = base64Decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPrivateKey key = (RSAPrivateKey) factory.generatePrivate(spec);
            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用公钥加密字符串
     * @param publicKey base64编码后的公钥
     * @param data 需要加密的数据
     * @return base64编码后的字符串
     */
    public static String encryptPublic(String publicKey, String data) {
        String encryptStr = "";
        RSAPublicKey key = getPublicKey(publicKey);
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptStr = base64Encode(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptStr;
    }

    /**
     * 使用私钥加密字符串
     * @param privateKey
     * @param data
     * @return
     */
    public static String encryptPrivate(String privateKey, String data) {
        String encryptStr = "";
        try {
            RSAPrivateKey key = getPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptStr = base64Encode(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptStr;
    }

    public static String decryptPublic(String publicKey, String data) {
        String decryptStr = "";
        try {
            RSAPublicKey key = getPublicKey(publicKey);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            //获取待解密的数据
            byte[] dataBytes = base64Decode(data);
            decryptStr = new String(cipher.doFinal(dataBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptStr;
    }

    public static String decryptPrivate(String privateKey, String data) {
        String decryptStr = "";
        try {
            RSAPrivateKey key = getPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            //获取需要解密的数据
            byte[] dataBytes = base64Decode(data);
            decryptStr = new String(cipher.doFinal(dataBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptStr;
    }

    /**
     * 使用私钥生成签名
     * @param privateKey
     * @param data
     * @return
     */
    public static String sign(String privateKey, String data) {
        try {
            Signature sign = Signature.getInstance(SIGN_ALGORITHM);
            sign.initSign(getPrivateKey(privateKey));
            sign.update(data.getBytes());
            return base64Encode(sign.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 使用公钥验证签名是否正确
     * @param publicKey
     * @param data
     * @return
     */
    public static boolean verifySign(String publicKey, String data, String signature) {
        try {
            Signature sign = Signature.getInstance(SIGN_ALGORITHM);
            sign.initVerify(getPublicKey(publicKey));
            sign.update(data.getBytes());
            return sign.verify(base64Decode(signature));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        KeyPair pair = generateRsaKeyPair();
        String publicKey = getPublicKey(pair);
        String privateKey = getPrivateKey(pair);

        System.out.println("this private key is :   " + privateKey);
        System.out.println("this public key is :    " + publicKey);
        System.out.println("------------------------------------------------");
        String data = "this is a message want to encrypt by publickey";
        String encryptStr = encryptPublic(publicKey, data);
        System.out.println("message be decrypt by private key, the message is [" + decryptPrivate(privateKey, encryptStr) + "]");

        System.out.println("------------------------------------------------");
        data = "this a message want to encrypt by privatekey";
        encryptStr = encryptPrivate(privateKey, data);
        System.out.println("message be decrypt by public key, the message is [" + decryptPublic(publicKey, encryptStr) + "]");

        System.out.println("------------------------------------------------");
        String signData = "this is a message to sign";
        String sign = sign(privateKey, signData);
        System.out.println("this signature verify :" + verifySign(publicKey, signData, sign));
    }
}
