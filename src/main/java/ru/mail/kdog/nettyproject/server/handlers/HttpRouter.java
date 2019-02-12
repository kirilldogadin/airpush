package ru.mail.kdog.nettyproject.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * этот класс в реальном приложении будет заниматься маппингом путей, http методов, etc
 */
public class HttpRouter extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {

        try {
       ctx.fireChannelRead(request);
        }
        finally {
            ReferenceCountUtil.release(request);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        cause.printStackTrace();
        ctx.close();
    }
}
