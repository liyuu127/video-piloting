package com.liyu.piloting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author liyu
 * date 2022/8/24 9:57
 * description 线路判断参数
 */
@Data
@ConfigurationProperties("piloting.judgment")
@Configuration
public class LineJudgmentConfig {


    private PositionConf position;
    private StartConf start;
    private EndConf end;
    private CameraConf camera;
    private StopConf stop;

    @Data
    class EndConf {

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

    public int getLineEndSatisfyDistanceCount() {
        return this.end.lineEndSatisfyDistanceCount;
    }

    public int getLineEndJudgmentPositionCount() {
        return this.end.lineEndJudgmentPositionCount;
    }

    public int getLineEndSatisfyDistanceMeter() {
        return this.end.lineEndSatisfyDistanceMeter;
    }

    @Data
    class CameraConf {
        /**
         * 判断拉取摄像头位置的数量
         */
        private int pullCameraJudgmentPositionCount = 5;
        /**
         * 拉取摄像头的符合距离的大小
         */
        private int pullCameraSatisfyDistanceMeter = 500;
        /**
         * 拉取摄像头的符合距离的位置个数，必须低于采集的数量
         */
        private int pullCameraSatisfyDistanceCount = 3;

        /**
         * 拉取摄像头方向时判断距离
         */
        private int pullCameraJudgmentIntervalMeter = 10;

        /**
         * 摄像头离开位置的数量
         */
        private int pullCameraOverPositionCount = 5;
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

    public int getPullCameraJudgmentPositionCount() {
        return this.camera.pullCameraJudgmentPositionCount;
    }

    public int getPullCameraSatisfyDistanceMeter() {
        return this.camera.pullCameraSatisfyDistanceMeter;
    }

    public int getPullCameraSatisfyDistanceCount() {
        return this.camera.pullCameraSatisfyDistanceCount;
    }

    public int getPullCameraJudgmentIntervalMeter() {
        return this.camera.pullCameraJudgmentIntervalMeter;
    }

    public int getPullCameraOverPositionCount() {
        return this.camera.pullCameraOverPositionCount;
    }

    public int getPullCameraDirectionScoreThreshold() {
        return this.camera.pullCameraDirectionScoreThreshold;
    }

    public int getCameraOverSatisfyDistanceMeter() {
        return this.camera.cameraOverSatisfyDistanceMeter;
    }

    public int getCameraOverSatisfyDistanceCount() {
        return this.camera.cameraOverSatisfyDistanceCount;
    }

    @Data
    class StartConf {

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

    public int getLineStartJudgmentPositionCount() {
        return this.start.lineStartJudgmentPositionCount;
    }

    public int getStartJudgmentIntervalMeter() {
        return this.start.startJudgmentIntervalMeter;
    }

    public int getStartDirectionScoreThreshold() {
        return this.start.startDirectionScoreThreshold;
    }

    @Data
    class PositionConf {
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

    public int getPositionExpireTime() {
        return this.position.positionExpireTime;
    }

    public int getPositionStoreInterval() {
        return this.position.positionStoreInterval;
    }

    public int getPositionQueueCapacity() {
        return this.position.positionQueueCapacity;
    }

    @Data
    class StopConf {
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

    public int getLineStopJudgmentPositionCount() {
        return this.stop.lineStopJudgmentPositionCount;
    }

    public double getLineStopJudgmentVariance() {
        return this.stop.lineStopJudgmentVariance;
    }

    public long getLineStopJudgmentTimeInterval() {
        return this.stop.lineStopJudgmentTimeInterval;
    }
}
