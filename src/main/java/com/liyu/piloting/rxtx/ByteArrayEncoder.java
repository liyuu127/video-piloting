package com.liyu.piloting.rxtx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liyu
 * date 2022/7/25 14:49
 * description
 */
@Slf4j
public class ByteArrayEncoder extends MessageToByteEncoder<byte[]> {

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        log.info(".....经过ByteArrayEncoder编码.....");
        //消息体，包含我们要发送的数据
        out.writeBytes(msg);
    }
}