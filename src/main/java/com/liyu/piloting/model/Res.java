package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/28 18:37
 * description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Res {

    private String msg;
    private String code;
    private String data;

}
