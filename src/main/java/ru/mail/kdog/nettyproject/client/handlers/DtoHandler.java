package ru.mail.kdog.nettyproject.client.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.mail.kdog.dto.DtoProtos;

public class DtoHandler extends SimpleChannelInboundHandler<DtoProtos.Page> {

    private static final Logger LOG = LogManager.getLogger(DtoHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DtoProtos.Page msg) {
        if (msg != null) {
            LOG.info("Message received " + msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Read channel error ",  cause);
        ctx.close();
    }
}