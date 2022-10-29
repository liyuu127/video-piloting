package com.liyu.piloting.service;

import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.model.LineInstance;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author liyu
 * date 2022/10/28 21:58
 * description
 */
@Service
@DependsOn({"lineConfig"})
public class LineService {
    @Getter
    @Setter
    private LineInstance lineInstance;
    @Autowired
    LineConfig lineConfig;

    @PostConstruct
    public void init() {
        this.lineInstance = lineConfig.lineInstance();
    }
}
