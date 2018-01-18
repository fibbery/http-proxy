package com.fibbery.handler;

import com.fibbery.bean.RequestProtocol;
import com.fibbery.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author fibbery
 * @date 18/1/17
 */
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            RequestProtocol protocol = RequestUtils.getRequestProtocol(request);
            //错误请求
            if (protocol == null) {
                ctx.channel().close();
                return;
            }

            HttpMethod requestMethod = request.method();
            //建立代理握手
            if (requestMethod == HttpMethod.CONNECT) {
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("codec");
                ctx.pipeline().remove("aggregator");
            }

            //连接目标服务器
            connectToTargetServer(ctx, request, protocol);
        }
    }

    private void connectToTargetServer(final ChannelHandlerContext ctx, final HttpRequest request, RequestProtocol protocol) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop()).
                channel(ctx.channel().getClass()).
                handler(new ClientChannelInitializer(ctx.channel()));
        ChannelFuture future = bootstrap.connect(protocol.getHost(), protocol.getPort());
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(request);
                } else {
                    ctx.channel().close();
                }
            }
        });
    }
}
