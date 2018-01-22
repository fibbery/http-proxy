package com.fibbery.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * @author fibbery
 * @date 18/1/18
 */
@Data
@NoArgsConstructor
public class ServerConfig {

    private X509Certificate clientCert; //用于代理客户机的证书

    private PrivateKey certPrivateKey; //证书对应的私钥

    private PrivateKey serverPrivateKey; //服务器持有的私钥

    private PublicKey serverPublicKey; // 服务器持有的公钥

}
