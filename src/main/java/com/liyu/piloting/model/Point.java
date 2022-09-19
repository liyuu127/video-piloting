package com.liyu.piloting.model;

import lombok.Data;

/**
 * @author liyu
 * date 2022/8/22 11:19
 * description
 */
@Data
public class Point {
    /**
     * 经度
     */
    private double longitude;
    /**
     * 维度
     */
    private double latitude;

    private double speed;

    private long timestamp;
}
