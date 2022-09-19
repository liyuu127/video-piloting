package com.liyu.piloting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/19 13:20
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndConf {

    /**
     * 到站有效位置数量
     */
    private int lineEndSatisfyDistanceCount = 5;

    /**
     * 终点判断位置数量
     */
    private int lineEndJudgmentPositionCount = 10;
    /**
     * 到站时站点距离
     */
    private int lineEndSatisfyDistanceMeter = 200;


}
