package com.liyu.piloting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author liyu
 * date 2022/9/19 13:24
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("piloting.alarm")
@Configuration
public class AlarmConf {


    /**
     * 查询前向时间（即告警过期时间）
     */
    private Long searchTimeForwardMillis;
    /**
     * 查询间隔时间
     */
    private Long searchTimeMillisInterval;
    private List<Integer> alarmType;
    private Integer status;


}
