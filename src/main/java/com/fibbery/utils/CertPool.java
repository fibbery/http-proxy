package com.fibbery.utils;

import com.fibbery.bean.ServerConfig;
import org.apache.commons.lang3.StringUtils;

import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fibbery
 * @date 18/1/23
 */
public class CertPool {

    private static ConcurrentHashMap<String, X509Certificate> certs = new ConcurrentHashMap<>();

    public static X509Certificate getCert(String host, ServerConfig config)throws Exception{
        if(StringUtils.isEmpty(host)) return null;
        X509Certificate cert = certs.get(host);
        if (cert == null) {
            certs.put(host, CertUtils.genCert(host, config.getIssuer(), config.getServerPrivateKey(), config.getServerPublicKey()));
            cert = certs.get(host);
        }
        return cert;
    }
}
