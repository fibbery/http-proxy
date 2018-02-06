package com.fibbery.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

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
        if (msg instanceof FullHttpMessage) {
            FullHttpMessage message = (FullHttpMessage) msg;
            message.headers().add("proxy", "test");
            String contentType = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
            if (StringUtils.isNotEmpty(contentType) && contentType.contains("json")) {
                String encoding = message.headers().get(HttpHeaderNames.CONTENT_ENCODING);
                //在原有的基础上增加proxy=test的属性
                JSONObject extData = new JSONObject();
                extData.put("proxy", "test");
                String newData;
                String originData = message.content().toString(CharsetUtil.UTF_8);
                if (originData.startsWith("[")) {
                    JSONArray array = JSON.parseArray(originData);
                    for (Object object : array) {
                        if (object instanceof JSONObject) {
                            JSONObject jsonObject = (JSONObject) object;
                            jsonObject.put("proxy", "test");
                        }
                    }
                    newData = array.toJSONString();
                } else {
                    JSONObject object = JSON.parseObject(originData);
                    object.put("proxy", "test");
                    newData = object.toJSONString();
                }

                //重新msg的conten属性
                ByteBuf content = clientChannel.alloc().compositeBuffer();
                content.writeBytes(newData.getBytes(CharsetUtil.UTF_8));
                ((FullHttpMessage) msg).headers().set(HttpHeaderNames.CONTENT_LENGTH, newData.getBytes(CharsetUtil.UTF_8).length);
                msg = ((FullHttpMessage) msg).replace(content);
            }
        }
        clientChannel.writeAndFlush(msg);
    }
}
