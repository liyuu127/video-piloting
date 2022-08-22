package com.liyu.piloting.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author liyu
 * date 2022/8/22 11:24
 * description
 */
@Data
@ConfigurationProperties("piloting.line")
@Configuration
public class LineConfig {
    private StationPosition startStation;
    private Set<Camera> cameraSet;
    private StationPosition endStation;
}
