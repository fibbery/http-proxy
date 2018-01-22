package com.fibbery.handler;

import com.fibbery.bean.ServerConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author fibbery
 * @date 18/1/17
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    private ServerConfig config;

    public ClientChannelHandler(Channel clientChannel, ServerConfig config) {
        this.clientChannel = clientChannel;
        this.config = config;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            response.headers().add("proxy", "test");
            if (msg instanceof FullHttpResponse) {
                FullHttpResponse fullResponse = (FullHttpResponse) msg;
                String contentType = fullResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
                System.out.println(contentType);
                if (contentType.contains("application/json")) {
                    System.out.println("this is a json reply");
                }
            }
            clientChannel.writeAndFlush(response);
        }
    }
}
