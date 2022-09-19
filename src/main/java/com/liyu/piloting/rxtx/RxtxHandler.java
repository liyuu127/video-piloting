package com.liyu.piloting.rxtx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * @author liyu
 * date 2022/7/25 14:50
 * description
 */
@ChannelHandler.Sharable
public class RxtxHandler extends SimpleChannelInboundHandler<byte[]> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        //文本方式编解码，String
        //System.out.println("接收到："+msg);

        // 十六进制发送编解码
        int dataLength = msg.length;
        ByteBuf buf = Unpooled.buffer(dataLength);
        buf.writeBytes(msg);
        System.out.println("接收到：");
        while (buf.isReadable()) {
            System.out.print(" " + buf.readByte());
        }
        System.out.println("");
        // 释放资源
        ReferenceCountUtil.release(msg);
    }
}
