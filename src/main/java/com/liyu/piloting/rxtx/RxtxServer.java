package com.liyu.piloting.rxtx;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * @author liyu
 * date 2022/7/25 14:52
 * description
 */
@Slf4j
@Component
public class RxtxServer {

    private RxtxChannel channel;

    @Autowired
    private SerialPortParam serialPortParam;

    private RxtxHandler handler = new RxtxHandler();

    public void createRxtx() throws Exception {
        // 串口使用阻塞io
        EventLoopGroup group = new OioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channelFactory(() -> {
                        RxtxChannel rxtxChannel = new RxtxChannel();
                        rxtxChannel.config()
                                .setBaudrate(serialPortParam.getBaudRate()) // 波特率
                                .setDatabits(serialPortParam.getDataBits()) // 数据位
                                .setParitybit(serialPortParam.getParity())      // 校验位
                                .setStopbits(serialPortParam.getStopBits()); // 停止位
                        return rxtxChannel;
                    })
                    .handler(new ChannelInitializer<RxtxChannel>() {
                        @Override
                        protected void initChannel(RxtxChannel rxtxChannel) {
                            rxtxChannel.pipeline().addLast(
//                                    new LineBasedFrameDecoder(60000),
                                    // 文本形式发送编解码
//                                    new StringEncoder(StandardCharsets.UTF_8),
//                                    new StringDecoder(StandardCharsets.UTF_8),
                                    // 十六进制形式发送编解码
                                    new ByteArrayDecoder(),
                                    new ByteArrayEncoder(),
                                    handler
                            );
                        }
                    });

            ChannelFuture f = bootstrap.connect(new RxtxDeviceAddress(serialPortParam.getSerialPortName())).sync();
            f.addListener(connectedListener);

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }


    // 连接监听
    GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) -> {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess()) {
            log.info("连接失败");
        } else {
            channel = (RxtxChannel) f.channel();
            log.info("连接成功");
            sendData();
        }
    };

    /**
     * 发送数据
     */
    public void sendData() {
        // 十六机制形式发送
        ByteBuf buf = Unpooled.buffer(2);
        buf.writeByte(3);
        buf.writeByte(2);
        channel.writeAndFlush(buf.array());

        // 文本形式发送
        //channel.writeAndFlush("2");
    }

    public void start() {
        CompletableFuture.runAsync(() -> {
            try {
                // 阻塞的函数
                createRxtx();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Executors.newSingleThreadExecutor());//不传默认使用ForkJoinPool，都是守护线程
    }

//    public static void main(String[] args) {
//        SerialPortParam serialPort = new SerialPortParam();
//        // 连接串口com1
//        serialPort.setSerialPortName("COM1");
//        serialPort.setBaudRate(9600);
//        RxtxServer rxtxServer = new RxtxServer();
//        rxtxServer.setSerialPortParam(serialPort);
//        rxtxServer.start();
//    }
}
