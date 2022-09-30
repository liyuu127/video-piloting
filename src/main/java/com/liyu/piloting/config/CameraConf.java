package com.liyu.piloting.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/19 13:22
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraConf {
    /**
     * 判断拉取摄像头位置的数量
     */
    private int pullCameraJudgmentPositionCount = 5;
    /**
     * 拉取摄像头的符合距离的大小
     */
    private int pullCameraSatisfyDistanceMeter = 1000;
    /**
     * 拉取摄像头的符合距离的位置个数，必须低于采集的数量
     */
    private int pullCameraSatisfyDistanceCount = 3;

    /**
     * 拉取摄像头方向时判断距离
     */
    private double pullCameraJudgmentIntervalMeter = 1;

    /**
     * 摄像头离开位置的数量
     */
    private int pullCameraOverPositionCount = 4;
    /**
     * 拉取摄像头方向计算得分，大于等于此值才判断分数有效
     */
    private int pullCameraDirectionScoreThreshold = 2;

    /**
     * 驶离多远算摄像头离站
     */
    private int cameraOverSatisfyDistanceMeter = 100;
    /**
     * 摄像头离站效位置数量
     */
    private int cameraOverSatisfyDistanceCount = 3;

}