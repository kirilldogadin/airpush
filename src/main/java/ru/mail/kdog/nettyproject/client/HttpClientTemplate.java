package ru.mail.kdog.nettyproject.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.mail.kdog.nettyproject.client.initializer.HttpProtoClientInitializer;
import ru.mail.kdog.nettyproject.server.HttpServerRunner;

import javax.net.ssl.SSLException;
import java.net.URI;

import static java.lang.Thread.sleep;

public class HttpClientTemplate {

    private static final Logger LOG = LogManager.getLogger(HttpServerRunner.class);

    private final URI uri;
    private Channel channel;
    private EventLoopGroup nioEventGroup;
    String scheme;
    String host;
    int port;

    public HttpClientTemplate(URI _uri) throws InterruptedException, SSLException {
        this.uri = _uri;
        scheme = uri.getScheme() == null ? "http" : uri.getScheme();
        host = uri.getHost() == null ? "localhost" : uri.getHost();
        port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            }
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            System.err.println("Only HTTP(S) is supported.");
            return;
        }

        final boolean ssl = "https".equalsIgnoreCase(scheme); //SSL context if necessary.
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        nioEventGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventGroup)
                .channel(NioSocketChannel.class)
                .handler(new HttpProtoClientInitializer(sslCtx));

        channel = bootstrap.connect(host, port).sync().channel();
    }

    public void send(FullHttpRequest request) {
        channel.writeAndFlush(request);
    }

    public void periodicSend(FullHttpRequest request, long requestTimeout) {
        while (!Thread.currentThread().isInterrupted()) { //можно убить kill -3
            request.retain();
            send(request);
            LOG.info("Send request to" + request);
            try {
                sleep(requestTimeout);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        LOG.info("Sending stopped");
    }

    public Channel getChannel() {
        return channel;
    }

    public void shutdownAll() throws InterruptedException {
        try {
            channel.closeFuture().sync();
            LOG.info("Connection close");
        } finally {
            nioEventGroup.shutdownGracefully();
            LOG.info("Client will be shutdown gracefully ");
        }
    }
}
