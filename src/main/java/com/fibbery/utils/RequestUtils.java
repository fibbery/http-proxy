package com.fibbery.utils;

import com.fibbery.bean.RequestProtocol;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fibbery
 * @date 18/1/17
 */
@Slf4j
public class RequestUtils {

    private final static int HTTPS_DEFAULT_PORT = 443;

    private final static int HTTP_DEFAULT_PORT = 80;

    public static RequestProtocol getRequestProtocol(HttpRequest request) {
        String hostStr = request.headers().get(HttpHeaderNames.HOST);
        if(StringUtils.isEmpty(hostStr)) return new RequestProtocol();
        String[] splitStr = hostStr.split(":");
        String host = splitStr[0];
        int port = splitStr.length > 1 ? Integer.parseInt(splitStr[1]) : HTTP_DEFAULT_PORT;
        return new RequestProtocol(host, port, port == HTTPS_DEFAULT_PORT);
    }
}
