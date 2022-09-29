package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author liyu
 * date 2022/9/28 18:37
 * description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmResp {

    private String msg;
    private String code;
    private List<Alarm> data;

    private Page page;

}
