package com.fibbery.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author fibbery
 * @date 18/1/17
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    public ClientChannelHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            response.headers().add("proxy", "test");
            clientChannel.writeAndFlush(response);
        }
    }
}
