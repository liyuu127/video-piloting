package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/28 19:16
 * description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    private Integer total;
    private Integer page;
    private Integer size;
}
