package ru.mail.kdog.nettyproject.client;

import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.mail.kdog.nettyproject.server.HttpServerRunner;

import java.net.URI;

public final class HttpClientRunner {

    private static final Logger LOG = LogManager.getLogger(HttpServerRunner.class);

    static final String URL = System.getProperty("url", "http://127.0.0.1:8080/");
    static final String REQ_TIMEOUT = System.getProperty("req_time", "50");

    public static void main(String[] args) throws Exception {
        URI uri = new URI(URL);
        long req_time = 50;
        try {
            req_time = Long.parseLong(REQ_TIMEOUT);
        } catch (IllegalArgumentException e) {
            LOG.warn("REQ_TIMEOUT WRONG. WILL BE USE DEFAULT VALUE  {} " + req_time);
        }

        //создание HTTP request.
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        HttpClientTemplate httpTemplate = new HttpClientTemplate(uri);

        httpTemplate.periodicSend(request, req_time);
        httpTemplate.shutdownAll();

    }
}