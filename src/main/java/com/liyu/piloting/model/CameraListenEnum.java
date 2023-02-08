package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum CameraListenEnum {
    LISTEN(1),
    QUERY(2),
    ;
    private int value;
}
