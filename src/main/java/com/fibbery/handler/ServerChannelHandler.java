package com.fibbery.handler;

import com.fibbery.bean.RequestProtocol;
import com.fibbery.bean.ServerConfig;
import com.fibbery.utils.CertPool;
import com.fibbery.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author fibbery
 * @date 18/1/17
 */
@Slf4j
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private ServerConfig config;

    private String host;

    private int port;

    /**
     * 判断是否处理了ssl握手请求
     */
    private boolean hasHandShake = false;

    public ServerChannelHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            RequestProtocol protocol = hasHandShake ? new RequestProtocol(host, port, true) : RequestUtils.getRequestProtocol(request);
            HttpMethod requestMethod = request.method();
            //建立代理握手
            if (requestMethod == HttpMethod.CONNECT) {
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK);
                this.host = protocol.getHost();
                this.port = protocol.getPort();
                ctx.writeAndFlush(response);
                ctx.channel().pipeline().remove("codec");
                ctx.channel().pipeline().remove("aggregator");
                return;
            }
            /**
             * 处理请求数据
             */
            handleProxyData(ctx.channel(), request, protocol);

        } else {
            //https://tools.ietf.org/html/rfc6101#section-5.1
            //ssl握手协议首字母是22
            ByteBuf buf = (ByteBuf) msg;
            if (buf.getByte(0) == 22) {
                SslContext context = SslContextBuilder.forServer(config.getServerPrivateKey(), CertPool.getCert(host, config)).build();
                ctx.channel().pipeline().addFirst("aggregator", new HttpObjectAggregator(65 * 1024));
                ctx.channel().pipeline().addFirst("httpCodec", new HttpServerCodec());
                ctx.channel().pipeline().addFirst("sslHandler", context.newHandler(ctx.channel().alloc()));
                hasHandShake = true;
                ctx.channel().pipeline().fireChannelRead(msg);
            }
        }
    }

    /**
     * 处理需要代理请求的数据
     * @param channel
     * @param msg
     */
    private void handleProxyData(Channel channel, HttpRequest msg, RequestProtocol protocol) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ClientChannelInitializer(channel, protocol.isSSL()));
        ChannelFuture future = bootstrap.connect(protocol.getHost(), protocol.getPort()).sync();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().writeAndFlush(msg);
            }
        });
        future.channel().closeFuture();
    }
}
