package com.liyu.piloting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/19 13:25
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionConf {
    /**
     * 过期期间间隔 ms
     */
    private int positionExpireTime = 2 * 1000;
    /**
     * 数据计算间隔 ms
     */
    private int positionStoreInterval = 1 * 1000;
    /**
     * 位置队列容量
     */
    private int positionQueueCapacity = 20;


}
