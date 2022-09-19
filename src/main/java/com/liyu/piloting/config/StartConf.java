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
public class StartConf {

    /**
     * 判断开始的位置的数量
     */
    private int lineStartJudgmentPositionCount = 4;
    /**
     * 开始移动时判断距离
     */
    private int startJudgmentIntervalMeter = 20;
    /**
     * 开始方向计算得分，大于等于此值才判断分数有效
     */
    private int startDirectionScoreThreshold = 2;


}
