package com.fibbery.utils;

import com.fibbery.bean.ServerConfig;
import org.apache.commons.lang3.StringUtils;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fibbery
 * @date 18/1/23
 */
public class CertPool {

    private static ConcurrentHashMap<String, X509Certificate> certs = new ConcurrentHashMap<>();

    public static X509Certificate getCert(String host, ServerConfig config)throws Exception{
        if(StringUtils.isEmpty(host)) return null;
        String key = host.trim().toLowerCase();
        X509Certificate cert = certs.get(key);
        if (cert == null) {
            String issuer = config.getClientCert().getIssuerDN().toString();
            Date notBefore = config.getClientCert().getNotBefore();
            Date notAfter = config.getClientCert().getNotAfter();
            cert = CertUtils.genCert(host, issuer, config.getCertPrivateKey(), config.getServerPublicKey(), notBefore, notAfter);
            certs.put(key, cert);
        }
        return cert;
    }
}
