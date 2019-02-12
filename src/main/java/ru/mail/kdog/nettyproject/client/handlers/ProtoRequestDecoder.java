package ru.mail.kdog.nettyproject.client.handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.mail.kdog.dto.DtoProtos;

import java.util.List;

public class ProtoRequestDecoder extends MessageToMessageDecoder<HttpContent> {

    private static final Logger LOG = LogManager.getLogger(DtoHandler.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpContent msg, List<Object> out) throws Exception {

        LOG.info("Message received");
        byte[] payloadBytes = new byte[msg.content().readableBytes()];
        msg.content().readBytes(payloadBytes);
        DtoProtos.Page page = null;
        try {
            page = DtoProtos.Page.parseFrom(payloadBytes);
        } catch (InvalidProtocolBufferException e) {
             LOG.error("Unable to parse message", e);
        }
        out.add(page);

    }
}