package com.fibbery.handler;

import com.fibbery.bean.RequestProtocol;
import com.fibbery.bean.ServerConfig;
import com.fibbery.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author fibbery
 * @date 18/1/17
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private ServerConfig config;

    public ServerChannelHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            RequestProtocol protocol = RequestUtils.getRequestProtocol(request);
            //错误请求
            if (protocol == null) {
                ctx.channel().close();
                return;
            } else if (request.uri() == "/bad-request") {
                return;
            }

            HttpMethod requestMethod = request.method();
            //建立代理握手
            if (requestMethod == HttpMethod.CONNECT) {
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("codec");
                ctx.pipeline().remove("aggregator");
                return;
            }
            //连接目标服务器
            connectToTargetServer(ctx, request, protocol);
        } else {
            //https请求无法解析，加入sslhandle之后重新再走一遍pipeline
            SslContext context = SslContextBuilder.forServer(config.getCertPrivateKey(), config.getClientCert()).build();
            ctx.pipeline().addFirst(context.newHandler(ctx.channel().alloc()));
            ctx.pipeline().fireChannelRead(msg);
        }
    }

    private void connectToTargetServer(final ChannelHandlerContext ctx, final HttpRequest request, RequestProtocol protocol) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop()).
                channel(ctx.channel().getClass()).
                handler(new ClientChannelInitializer(ctx.channel(), config, protocol.isSSL()));
        ChannelFuture channelFuture = bootstrap.connect(protocol.getHost(), protocol.getPort());
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush(request);
            } else {
                ctx.channel().close();
            }
        });
    }
}
