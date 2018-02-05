package SSLDemo;

import com.fibbery.utils.CertUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

/**
 * @author fibbery
 * @date 18/2/5
 */
public class DynamicCertificate {

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();


        //load ca cert
        X509Certificate caCertificate = CertUtils.loadCert(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca.crt"));
        RSAPrivateKey caPrivateKey= CertUtils.loadPrivateKey(Thread.currentThread().getContextClassLoader().getResourceAsStream("ca_private_pkcs8.pem"));
        String issuer =
                caCertificate.getIssuerDN().toString();
        X509Certificate dynamicCertificate = CertUtils.genCert(
                "www.baidu.com",
                issuer,
                caPrivateKey,
                publicKey,
                caCertificate.getNotBefore(),
                caCertificate.getNotAfter()
        );

        //输出文件
        String content = Base64.getEncoder().encodeToString(dynamicCertificate.getEncoded());
        BufferedWriter writer = new BufferedWriter(new FileWriter("dynamic.crt"));
        writer.write("-----BEGIN CERTIFICATE-----");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        byte[] buffer = new byte[64];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            writer.newLine();
            writer.write(new String(buffer), 0, length);
        }
        writer.newLine();
        writer.write("-----END CERTIFICATE-----");
        writer.close();
    }
}
