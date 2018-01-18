package com.fibbery.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.security.KeyStore;


/**
 * @author fibbery
 * @date 18/1/17
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("jks");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyserver.jks");
        ks.load(in, "nettyserver".toCharArray());
        kmf.init(ks, "nettyserver".toCharArray());
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(kmf.getKeyManagers(), null, null);
        SSLEngine sslEngine = context.createSSLEngine();
        sslEngine.setUseClientMode(false);

        ch.pipeline().addFirst("ssl", new SslHandler(sslEngine));
        ch.pipeline().addLast("codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536)); //64k
        ch.pipeline().addLast("handler", new ServerChannelHandler());
    }
}
