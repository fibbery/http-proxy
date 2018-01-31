package com.fibbery.handler;

import com.fibbery.bean.ServerConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;


/**
 * @author fibbery
 * @date 18/1/17
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ServerConfig config;

    public ServerChannelInitializer(ServerConfig config) {
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
//        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//        KeyStore ks = KeyStore.getInstance("jks");
//        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyserver.jks");
//        ks.load(in, "nettyserver".toCharArray());
//        kmf.init(ks, "nettyserver".toCharArray());
//        SSLContext context = SSLContext.getInstance("SSL");
//        context.init(kmf.getKeyManagers(), null, null);
//        SSLEngine sslEngine = context.createSSLEngine();
//        sslEngine.setUseClientMode(false);
//        ------------------------------------------------------
//        InputStream certIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("rsa_public_key.crt");
//        InputStream keyIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("rsa_private_key_pkcs8.pem");
//        SslContext sslContext = SslContextBuilder.forServer(certIn, keyIn).build();
//        SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
//        sslEngine.setUseClientMode(false);
        ch.pipeline().addLast("codec", new HttpServerCodec());
        //ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536)); //64k
        ch.pipeline().addLast("handler", new ServerChannelHandler(config));
    }
}
