package com.liyu.piloting.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author liyu
 * date 2022/8/22 11:33
 * description
 */
@Data
public class LineInstance {
    private static int LINE_STATUS_INIT = 1;
    /**
     * 进入终点站
     */
    private static int LINE_STATUS_START = 2;
    /**
     * 摄像头全部拉取完
     */
    private static int LINE_STATUS_ALL_PULL = 3;
    /**
     * 终点站
     */
    private static int LINE_STATUS_END = 4;
    /**
     * 停车
     */
    private static int LINE_STATUS_STOP = 5;

    private static int LINE_DIRECTION_POSITIVE = 1;
    private static int LINE_DIRECTION_NEGATIVE = -1;

    private StationPosition startStation;
    private List<Camera> cameraList;
    private StationPosition endStation;
    /**
     * 1 正向，-1：反向
     */
    private Integer direction;
    private Long directionTimestamp;
    /**
     * 上个拉流地址
     */
    private Camera lastCamera;
    private Camera nowCamera;
    private Camera nextCamera;

    /**
     * 线路状态
     */
//    private Integer status;
    public boolean directionIsPositive() {
        return this.direction == LINE_DIRECTION_POSITIVE;

    }

    public void directionPositive() {
        this.directionTimestamp = System.currentTimeMillis();
        this.direction = LINE_DIRECTION_POSITIVE;
    }

    public void directionNegative() {
        this.directionTimestamp = System.currentTimeMillis();
        this.direction = LINE_DIRECTION_NEGATIVE;
    }

//    public void lineStatusInit() {
//        this.status = LINE_STATUS_INIT;
//
//    }
//
//    public void lineStatusStart() {
//        this.status = LINE_STATUS_START;
//
//    }
//
//    public void lineStatusEnd() {
//        this.status = LINE_STATUS_END;
//    }
//
//    public boolean lineStatusIsInit() {
//        return this.status == LINE_STATUS_INIT;
//
//    }
//
//    public boolean lineStatusIsStart() {
//        return this.status == LINE_STATUS_START;
//
//    }
//
//    public boolean lineStatusIsEnd() {
//        return this.status == LINE_STATUS_END;
//    }
//
//    public boolean lineStatusIsAllPull() {
//        return this.status == LINE_STATUS_ALL_PULL;
//    }
//
//    public void lineStatusAllPull() {
//        this.status = LINE_STATUS_ALL_PULL;
//    }

}
