package ru.mail.kdog.nettyproject.server.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import ru.mail.kdog.nettyproject.server.handlers.HttpResponseCreator;
import ru.mail.kdog.nettyproject.server.handlers.HttpRouter;
import ru.mail.kdog.nettyproject.server.handlers.ServiceHandler;

public class HttpProtoServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    static final int COUNT_EVENT_GROUP_THREAD = 1;
    static final EventExecutorGroup group = new DefaultEventExecutorGroup(COUNT_EVENT_GROUP_THREAD);
    static final int MAX_CONTENT_LENGTH = 1048576;


    public HttpProtoServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new LoggingHandler(LogLevel.INFO));
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        //p.addLast(new HttpContentCompressor());
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new HttpResponseCreator());
        p.addLast(new ProtobufEncoder());

        p.addLast(new HttpRouter());
        p.addLast(group,"blocking-handler",new ServiceHandler());
    }
}