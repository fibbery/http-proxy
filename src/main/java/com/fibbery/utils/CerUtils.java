package com.fibbery.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 工具类针对的是openss生成的密钥和证书
 * # 生成私钥
 * 1. openssl genrsa -out rsa_private_key.pem 1024
 * # 根据私钥生成公钥
 * 2. openssl rsa -in rsa_private_key.pem -out rsa_public_key.pem -outform PEM -pubout
 * # java不能直接读取该私钥，需要转换成pkcs8格式
 * 3. openssl pkcs8 -topk8 -inform PEM -in rsa_private_key.pem -outform PEM -nocrypt -out rsa_private_key_pkcs8.pem
 * # 根据私钥创建证书请求
 * 4. openssl req -new -key rsa_private_key.pem -out rsa_public_key.csr
 * # 生成证书并且签名
 * 5. openssl x509 -req -days 3650 -in rsa_public_key.csr -signkey rsa_private_key.pem -out rsa_public_key.crt
 * @author fibbery
 * @date 18/1/19
 **/
public class CerUtils {

    /**
     * 从文件流中导入证书信息
     *
     * @param in
     * @return
     * @throws Exception
     */
    public static X509Certificate loadCert(InputStream in) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(in);
    }

    /**
     * 从pkcs8格式的密钥文件从读取出私钥
     *
     * @param in
     * @return 私钥
     */
    public static RSAPrivateKey loadPrivateKey(InputStream in) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(readKeyStr(in));
        KeyFactory factory = KeyFactory.getInstance("rsa");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    /**
     * 从文件流中加载公钥
     *
     * @param in
     * @return
     * @throws Exception
     */
    public static RSAPublicKey loadPublicKey(InputStream in) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(readKeyStr(in));
        KeyFactory factory = KeyFactory.getInstance("rsa");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return (RSAPublicKey) factory.generatePublic(spec);
    }

    private static String readKeyStr(InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.charAt(0) != '-') {
                buffer.append(line);
            }
        }
        return buffer.toString();
    }

    public static void main(String[] args) throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey(Thread.currentThread().getContextClassLoader().getResourceAsStream("rsa_private_key_pkcs8.pem"));
        RSAPublicKey publicKey = loadPublicKey(Thread.currentThread().getContextClassLoader().getResourceAsStream("rsa_public_key.pem"));
        String data = "test message";
        KeyPair pair = new KeyPair(publicKey, privateKey);
        String privateKeyStr = RsaUtils.getPrivateKey(pair);
        String publicKeyStr = RsaUtils.getPublicKey(pair);
        String encryptStr = RsaUtils.encryptPrivate(privateKeyStr, data);
        System.out.println("the encrypt message is : " + RsaUtils.decryptPublic(publicKeyStr, encryptStr));

        X509Certificate x509Certificate = loadCert(Thread.currentThread().getContextClassLoader().getResourceAsStream("rsa_public_key.crt"));
        int version = x509Certificate.getVersion();
        System.out.println(version);
    }
}
