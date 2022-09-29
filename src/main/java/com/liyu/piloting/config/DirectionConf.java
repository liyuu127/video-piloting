package com.liyu.piloting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/19 13:24
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectionConf {

    /**
     * 判断方向的位置的数量
     */
    private int directionJudgmentPositionCount = 4;
    /**
     * 开始移动时判断距离
     */
    private int directionJudgmentIntervalMeter = 15;
    /**
     * 开始方向计算得分，大于等于此值才判断分数有效
     */
    private int directionScoreThreshold = 3;

    /**
     * 多久间隔计算一次方向 默认一分钟
     */
    private long directionCalculateInterval = 1 * 1000 * 60;

}
