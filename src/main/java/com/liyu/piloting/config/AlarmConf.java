package com.liyu.piloting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Long firstDelayTime;
    private Long searchTimeMillisInterval;
    private Long alarmInterval;
    private Long noalarmInterval;
    private List<Integer> alarmType;
    private List<Integer> dwAlarmType;
    private Integer status;
    private Integer model;
    private Integer cameraListen;
    private Long cameraUpInterval = 5000l;
    private Long cameraDownInterval = 5000l;
    private Boolean dwCheckOnlineEnable = false;
    private Long dwCheckOnlineTimeout = 3000l;


}
