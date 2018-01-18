package com.fibbery.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fibbery
 * @date 18/1/18
 */
@Slf4j
public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        log.info("============================");
        FullHttpResponse response = (FullHttpResponse) msg;

        //print headers
        if (!response.headers().isEmpty()) {
            log.info("-----------Headers start------------");
            response.headers().entries().forEach(entry -> {
                log.info(entry.getKey() + " : " + entry.getValue());
            });
            log.info("-----------Headers end------------");
        }

        //print contetnt
        log.info("-----------Data start------------");
        if (HttpHeaders.isTransferEncodingChunked(response)) {
            log.info("chunked content");
        } else {
            log.info(response.content().toString(CharsetUtil.UTF_8));
        }
        log.info("-----------Data end------------");
    }
}
