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
    private static int LINE_STATUS_START = 2;
    private static int LINE_STATUS_ALL_PULL = 3;
    private static int LINE_STATUS_END = 4;

    private static int LINE_DIRECTION_POSITIVE = 1;
    private static int LINE_DIRECTION_NEGATIVE = 2;

    private StationPosition startStation;
    private List<Camera> cameraList;
    private StationPosition endStation;
    /**
     * 1 正向，2：反向
     */
    private Integer direction;
    /**
     * 上个拉流地址
     */
    private Camera lastCamera;
    private Camera nowCamera;

    /**
     * 线路状态
     */
    private Integer status;


    public boolean directionIsPositive() {
        return this.direction == LINE_DIRECTION_POSITIVE;
    }

    public void directionPositive() {
        this.direction = LINE_DIRECTION_POSITIVE;
    }

    public void directionNegative() {
        this.direction = LINE_DIRECTION_NEGATIVE;
    }

    public void lineStatusInit() {
        this.status = LINE_STATUS_INIT;

    }

    public void lineStatusStart() {
        this.status = LINE_STATUS_START;

    }

    public void lineStatusEnd() {
        this.status = LINE_STATUS_END;
    }

    public boolean lineStatusIsInit() {
        return this.status == LINE_STATUS_INIT;

    }

    public boolean lineStatusIsStart() {
        return this.status == LINE_STATUS_START;

    }

    public boolean lineStatusIsEnd() {
        return this.status == LINE_STATUS_END;
    }

    public boolean lineStatusIsAllPull() {
        return this.status == LINE_STATUS_ALL_PULL;
    }

    public void lineStatusAllPull() {
        this.status = LINE_STATUS_ALL_PULL;
    }

}
