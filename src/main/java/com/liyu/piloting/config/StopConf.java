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
public class StopConf {
    /**
     * 判断停车的位置的数量
     */
    private int lineStopJudgmentPositionCount = 15;
    /**
     * 停车位置数据的方差大小 小于等于此值算停车
     */
    private double lineStopJudgmentVariance = 2;
    /**
     * 停车位置计算时间间隔
     */
    private long lineStopJudgmentTimeInterval = 5 * 1000;


}
