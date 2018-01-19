package SSLDemo;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

/**
 *  提取jks文件中的密钥和公钥进行签名验证
 * @author fibbery
 * @date 18/1/19
 */
public class SignAndVerify{

    public static String byteToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            int tmp = b & 0xff;
            String tmpStr = Integer.toHexString(tmp);
            if (tmpStr.length() < 2) {
                sb.append("0");
            }
            sb.append(tmpStr);
        }
        return sb.toString();
    }

    public static byte[] hexToByte(String data) {
        byte ret[] = new byte[data.length() / 2];
        for (int i = 0; i < ret.length; i ++) {
            int index = i * 2;
            int tmp = Integer.parseInt(data.substring(index, index + 2), 16);
            ret[i] = (byte) tmp;
        }
        return ret;
    }

    public static String signature(String data) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, InvalidKeyException, SignatureException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyserver.jks");
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(in, "nettyserver".toCharArray());
        PrivateKey privateKey= (PrivateKey) ks.getKey("nettyserver", "nettyserver".toCharArray());
        //用私钥对数据进行签名
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] sign = signature.sign();
        return byteToHex(sign);
    }

    public static boolean verify(String originData, String sign) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        KeyStore ks = KeyStore.getInstance("jks");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyserver_pub.jks");
        ks.load(in,"nettyserver".toCharArray());
        PublicKey publicKey = ks.getCertificate("nettyserver").getPublicKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(originData.getBytes());
        byte[] bytes = hexToByte(sign);
        return signature.verify(bytes);
    }

    public static void main(String[] args) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, SignatureException, InvalidKeyException, IOException {
        String originData = "test";
        String signature = signature(originData);
        System.out.println(verify(originData, signature));
    }


}
