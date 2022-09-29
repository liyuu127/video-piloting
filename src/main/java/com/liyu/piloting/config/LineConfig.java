package com.liyu.piloting.config;

import com.liyu.piloting.model.Camera;
import com.liyu.piloting.model.LineInstance;
import com.liyu.piloting.model.StationPosition;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
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
    private List<Camera> cameraList;
    private StationPosition endStation;


    public LineInstance lineInstance() {
        LineInstance lineInstance = new LineInstance();
        lineInstance.setStartStation(startStation.clone());
        lineInstance.setEndStation(endStation.clone());
        List<Camera> cameraList = new ArrayList<>();
        for (Camera camera : this.cameraList) {
            cameraList.add(camera.clone());
        }
        lineInstance.setCameraList(cameraList);

//        lineInstance.directionPositive();
        return lineInstance;
    }
}
