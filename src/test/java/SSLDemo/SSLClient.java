package SSLDemo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author fibbery
 * @date 18/1/18
 */
public class SSLClient {

    public static void startClient() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyclient.jks");
        ks.load(in, "nettyclient".toCharArray());
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, tmf.getTrustManagers(), null);
        final SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);

        //客户端
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addFirst(new SslHandler(engine));
            }
        });
        ChannelFuture future = bootstrap.connect("127.0.0.1", 9999).sync();
        future.channel().closeFuture().sync();
    }

    public static void main(String[] args) throws Exception {
        startClient();
    }
}
