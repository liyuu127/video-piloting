package com.liyu.piloting.rxtx;

import com.liyu.piloting.service.PositionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liyu
 * date 2022/7/25 14:50
 * description
 */
@ChannelHandler.Sharable
@Slf4j
@Component
public class RxtxHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    PositionService positionService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        if (StringUtils.isNotBlank(msg)) {
            positionService.processMsg(msg);
        }
        // 释放资源
        ReferenceCountUtil.release(msg);
    }
}
