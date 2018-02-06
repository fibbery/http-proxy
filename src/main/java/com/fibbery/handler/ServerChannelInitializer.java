package com.fibbery.handler;

import com.fibbery.bean.ServerConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;


/**
 * @author fibbery
 * @date 18/1/17
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ServerConfig config;

    public ServerChannelInitializer(ServerConfig config) {
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536)); //64k
        ch.pipeline().addLast("handler", new ServerChannelHandler(config));
    }
}
