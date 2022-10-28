package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/10/28 15:50
 * description
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum AlarmModelEnum {
    YS7(1),
    HK(2),
    ;
    private int model;
}
