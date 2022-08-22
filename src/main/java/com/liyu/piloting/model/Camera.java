package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/8/22 11:25
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Camera implements Cloneable {

    private final static int STATUS_UN_PULL = 1;
    private final static int STATUS_PULLED = 2;
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
    private Integer status = 1;

    public void statusUnPull() {
        this.status = STATUS_UN_PULL;
    }

    public void statusPull() {
        this.status = STATUS_PULLED;
    }

    public boolean statusIsUnPull() {
        return this.status == STATUS_UN_PULL;
    }

    public boolean statusIsPull() {
        return this.status == STATUS_PULLED;
    }

    @Override
    public Camera clone() {
        try {
            return (Camera) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Camera(this.longitude, this.latitude, this.name, this.url, this.status);
        }
    }
}
