package SSLDemo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author fibbery
 * @date 18/1/18
 */
public class SSLServer {

    public static void startServer()throws Exception {
        KeyManagerFactory kmf = null;
        KeyStore ks = KeyStore.getInstance("jks");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyserver.jks");
        ks.load(in, "nettyserver".toCharArray());
        kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "nettyserver".toCharArray());
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(kmf.getKeyManagers(), null, null);
        final SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);

        //服务端启动
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addFirst(new SslHandler(engine));
            }
        });
        ChannelFuture future = bootstrap.bind("127.0.0.1", 9999).sync();
        future.channel().closeFuture().sync();
    }



    public static void main(String[] args) throws Exception {
        startServer();
    }
}
