package com.fibbery.handler;

import com.fibbery.bean.RequestProtocol;
import com.fibbery.bean.ServerConfig;
import com.fibbery.utils.CertPool;
import com.fibbery.utils.RequestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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

    public ServerChannelHandler(ServerConfig config) {
        this.config = config;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            RequestProtocol protocol = RequestUtils.getRequestProtocol(request);
            //错误请求
            if (StringUtils.isEmpty(protocol.getHost())) {
                ctx.channel().close();
                return;
            }

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
            //连接目标服务器
            log.info(">>>>>>>>>>>>>> request uri is : " + request.uri());
            connectToTargetServer(ctx, request, protocol);
        } else {
            //https://tools.ietf.org/html/rfc6101#section-5.1
            //ssl握手协议首字母是22
            ByteBuf buf = (ByteBuf) msg;
            if (buf.getByte(0) == 22) {
                log.info("-----------handshake");
                SslContext context = SslContextBuilder.forServer(config.getServerPrivateKey(), CertPool.getCert(host, config)).build();
                ctx.channel().pipeline().addFirst(context.newHandler(ctx.channel().alloc()));
                ctx.channel().pipeline().fireChannelRead(msg);
                return;
            }
            handleProxyData(ctx.channel(), msg);
        }
    }

    /**
     * 处理需要代理请求的数据
     * @param channel
     * @param msg
     */
    private void handleProxyData(Channel channel, Object msg) {
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
