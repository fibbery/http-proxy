package com.fibbery.utils;

import com.fibbery.bean.RequestProtocol;
import com.fibbery.handler.HttpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 本例采用的jdk自带的keytool生成的证书
 * @author fibbery
 * @date 18/1/18
 */
public class HttpClient {

    public static HttpRequest getRequestMethod(String url, HttpMethod method, Map<String, String> parameters) throws HttpPostRequestEncoder.ErrorDataEncoderException {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String host = uri.getHost();
        String rawPath = uri.getRawPath();

        HttpRequest request = null;
        //GET POST只有url构造不同而已
        if (method == HttpMethod.POST) {
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, rawPath);
            HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
            final HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(factory, request, false);
            if (parameters != null) {
                parameters.forEach((name, value) -> {
                    try {
                        encoder.addBodyAttribute(name, value);
                    } catch (HttpPostRequestEncoder.ErrorDataEncoderException e) {
                        e.printStackTrace();
                    }
                });
                request = encoder.finalizeRequest();
            }
        }else{
            QueryStringEncoder encoder = new QueryStringEncoder(uri.toString());
            if (parameters != null) {
                parameters.forEach(encoder::addParam);
            }
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, encoder.toString());
        }

        // add headers
        request.headers().add(HttpHeaders.Names.HOST, host);
        request.headers().add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        request.headers().add(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP_DEFLATE);
        request.headers().add(HttpHeaders.Names.USER_AGENT, "Netty Simple Http Client");
        request.headers().add(HttpHeaders.Names.ACCEPT_ENCODING, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.headers().add(HttpHeaders.Names.CONNECTION, ClientCookieEncoder.LAX.encode(
                new DefaultCookie("my-cookie", "foo"),
                new DefaultCookie("another-cookie", "bar")
        ));
        return request;
    }

    public static void run(HttpRequest request) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            RequestProtocol protocol = RequestUtils.getRequestProtocol(request);
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new HttpClientChannelInitializer(protocol));
            ChannelFuture future = bootstrap.connect(protocol.getHost(), protocol.getPort()).sync();
            future.channel().writeAndFlush(request).sync();
            future.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }

    }


    public static class HttpClientChannelInitializer extends ChannelInitializer<SocketChannel>{

        private RequestProtocol protocol;

        public HttpClientChannelInitializer(RequestProtocol protocol) {
            this.protocol = protocol;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            if (protocol.isSSL()) {
//                使用keytool
//                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
//                KeyStore ks = KeyStore.getInstance("JKS");
//                InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("nettyclient.jks");
//                ks.load(in, "nettyclient".toCharArray());
//                tmf.init(ks);
//
//                SSLContext context = SSLContext.getInstance("SSL");
//                context.init(null, tmf.getTrustManagers(), null);
//                final SSLEngine engine = context.createSSLEngine();
//                engine.setUseClientMode(true);
                SslContextBuilder contextBuilder= SslContextBuilder.forClient();
                SSLEngine engine = contextBuilder.build().newEngine(UnpooledByteBufAllocator.DEFAULT);
                ch.pipeline().addFirst(new SslHandler(engine));
            }
            ch.pipeline().addLast("httpcodec", new HttpClientCodec());
            ch.pipeline().addLast("inflater", new HttpContentDecompressor());
            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(1024 * 1024));
            ch.pipeline().addLast("handler", new HttpClientHandler());

        }
    }

    public static void main(String[] args) throws Exception{
        HashMap<String, String> params = new HashMap<>();
        HttpRequest requestMethod = getRequestMethod("https://api.douban.com/v2/user/~me", HttpMethod.GET, params);
        run(requestMethod);
    }
}
