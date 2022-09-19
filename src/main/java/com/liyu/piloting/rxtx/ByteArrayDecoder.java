package com.liyu.piloting.rxtx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author liyu
 * date 2022/7/25 14:48
 * description
 */
public class ByteArrayDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 标记一下当前的readIndex的位置
        in.markReaderIndex();
        int dataLength = in.readableBytes();
        byte[] array = new byte[dataLength];
        in.readBytes(array, 0, dataLength);
        if (array.length > 0) {
            out.add(array);
        }
    }
}
