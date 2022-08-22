package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/8/22 11:22
 * description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationPosition implements Cloneable {
    /**
     * 经度
     */
    private double longitude;
    /**
     * 维度
     */
    private double latitude;

    private String name;

    @Override
    public StationPosition clone() {
        try {
            return (StationPosition) super.clone();
        } catch (CloneNotSupportedException e) {
            return new StationPosition(this.longitude, this.latitude, this.name);
        }
    }
}