package com.liyu.piloting.rxtx;

import io.netty.channel.rxtx.RxtxChannelConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author liyu
 * date 2022/7/25 14:51
 * description
 */
@Data
@ConfigurationProperties("piloting.rxtx")
@Configuration
public class SerialPortParam {

    /**
     * 串口名称，以COM开头(COM0、COM1、COM2等等)
     */
    private String serialPortName;
    /**
     * 波特率， 默认：9600
     */
    private int baudRate = 9600;
    /**
     * 数据位 默认8位
     * 可以设置的值：SerialPort.DATABITS_5、SerialPort.DATABITS_6、SerialPort.DATABITS_7、SerialPort.DATABITS_8
     */
    private RxtxChannelConfig.Databits dataBits = RxtxChannelConfig.Databits.DATABITS_8;
    /**
     * 停止位
     * 可以设置的值：SerialPort.STOPBITS_1、SerialPort.STOPBITS_2、SerialPort.STOPBITS_1_5
     */
    private RxtxChannelConfig.Stopbits stopBits = RxtxChannelConfig.Stopbits.STOPBITS_1;
    /**
     * 校验位
     * 可以设置的值：SerialPort.PARITY_NONE、SerialPort.PARITY_ODD、SerialPort.PARITY_EVEN、SerialPort.PARITY_MARK、SerialPort.PARITY_SPACE
     */
    private RxtxChannelConfig.Paritybit parity = RxtxChannelConfig.Paritybit.NONE;
}
