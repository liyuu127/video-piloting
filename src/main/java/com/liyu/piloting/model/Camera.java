package com.liyu.piloting.model;

import lombok.Data;

/**
 * @author liyu
 * date 2022/8/22 11:25
 * description
 */
@Data
public class Camera {
    /**
     * 经度
     */
    private Double longitude;
    /**
     * 维度
     */
    private Double latitude;

    private String name;

    private String url;

    /**
     * 0：取消，1：未拉流，2：已拉流
     */
    private int status;
}
