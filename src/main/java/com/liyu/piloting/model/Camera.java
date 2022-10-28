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
    private final static int STATUS_PULL_OVER = 3;
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
    private String deviceSerial;

    /**
     * 0：取消，1：未拉流，2：已拉流
     */
    @Deprecated
    private Integer status = 1;
    //用于连接
    private int id;
    private String ip;
    private short port;
    private String user;
    private String psw;
    private String sSerialNumber;



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

    public void statusPullOver() {
        this.status = STATUS_PULL_OVER;
    }

    public boolean statusIsPullOver() {
        return this.status == STATUS_PULL_OVER;
    }

    @Override
    public Camera clone() {
        try {
            return (Camera) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Camera(this.longitude, this.latitude, this.name, this.url, this.deviceSerial, this.status, this.id,this.ip, this.port, this.user, this.psw,this.sSerialNumber);
        }
    }
}
