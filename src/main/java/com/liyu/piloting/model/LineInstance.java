package com.liyu.piloting.model;

import lombok.Data;

import java.util.Set;

/**
 * @author liyu
 * date 2022/8/22 11:33
 * description
 */
@Data
public class LineInstance {
    private StationPosition startStation;
    private Set<Camera> cameraSet;
    private StationPosition endStation;
    /**
     * 1 正向，2：反向
     */
    private int direction;
    /**
     * 上个拉流地址
     */
    private Camera lastCamera;
}
