package com.fibbery.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 *
 * @author fibbery
 * @date 18/1/19
 **/
public class CertUtils {

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
     * 使用crypto workshop动态生成证书
     * @param host 域名
     * @param issuer 签发者信息
     * @param caPrivateKey CA证书私钥
     * @param publicKey 生成证书的公钥
     * @param notBefore 过期时间(起)
     * @param notAfter 过期时间(终)
     * @return
     * @throws Exception
     */
    public static X509Certificate genCert(String host, String issuer, PrivateKey caPrivateKey, PublicKey publicKey, Date notBefore, Date notAfter) throws Exception {
        String subject = issuer.replaceAll("CN=[a-zA-Z]+", "CN=" + host);
        //serial采用时间戳+4位随机数避免验证出现证书不安全的问题
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                new X500Name(issuer),
                BigInteger.valueOf(System.currentTimeMillis() + (long) (Math.random() * 10000) + 1000),
                notBefore,
                notAfter,
                new X500Name(subject),
                publicKey
        );

        //增加SNA拓展，防止浏览器提示证书不安全
        GeneralName generalName = new GeneralName(GeneralName.dNSName, host);
        GeneralNames names = new GeneralNames(generalName);
        builder.addExtension(Extension.subjectAlternativeName, false, names);

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WITHRSAENCRYPTION").build(caPrivateKey);
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
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



    public static String getIssuer(X509Certificate cert) {
        List<String> tempList = Arrays.asList(cert.getIssuerDN().toString().split(", "));
        return IntStream.rangeClosed(0, tempList.size() - 1)
                .mapToObj(i -> tempList.get(tempList.size() - i - 1)).collect(Collectors.joining(", "));
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
        String issuer = "C=CN, ST=Guangdong, L=Shenzhen, O=jiangnenghua, OU=study, CN=jiangnenghua, EMAIL=jiangnenghua1992@gmail.com";
        String replace = issuer.replaceAll("CN=[a-zA-Z]+", "CN=host");
        System.out.println(replace);
    }
}
