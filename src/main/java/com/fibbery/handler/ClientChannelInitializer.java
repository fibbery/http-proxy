package com.fibbery.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;


/**
 * @author fibbery
 * @date 18/1/17
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Channel clientChannel;

    public ClientChannelInitializer(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("codec", new HttpClientCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        ch.pipeline().addLast("handler", new ClientChannelHandler(clientChannel));
    }
}
