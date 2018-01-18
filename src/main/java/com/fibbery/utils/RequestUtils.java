package com.fibbery.utils;

import com.fibbery.bean.RequestProtocol;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fibbery
 * @date 18/1/17
 */
@Slf4j
public class RequestUtils {

    private final static int HTTPS_DEFAULT_PORT = 443;

    private final static int HTTP_DEFAULT_PORT = 80;

    public static RequestProtocol getRequestProtocol(HttpRequest request) {
        String uri = request.uri();
        String host = StringUtils.EMPTY;
        int port = HTTP_DEFAULT_PORT;
        boolean isSSL = false;
        Pattern pattern = Pattern.compile("^(?:https?://)?(?<host>[^/]*)/?.*$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            String hostStr = matcher.group("host");
            int index = hostStr.indexOf(":");
            host = index != -1 ? uri.substring(0, index) : hostStr;
            port = index != -1 ? Integer.parseInt(hostStr.substring(index + 1, hostStr.length())) : (uri.contains("https") ? HTTPS_DEFAULT_PORT : HTTP_DEFAULT_PORT);
            isSSL = port == HTTPS_DEFAULT_PORT;
        }
        RequestProtocol protocol = new RequestProtocol(host, port, isSSL);
        log.info(protocol.toString());
        return protocol;
    }
}
