package com.liyu.piloting.service;

import com.liyu.piloting.model.Point;
import com.liyu.piloting.util.EarthMapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liyu
 * date 2022/8/23 9:34
 * description
 */
@Service
@Slf4j(topic = "PositionTest")
public class PositionTest {

    @Autowired
    PilotingService pilotingService;

    /**
     * 发送两个点间的经纬度信息
     */
    public void sendPoint(Point start, Point end, double lon_t, double lat_t, long timestamp_t) {

        long startTimestamp = System.currentTimeMillis();
        long lastTimestamp = startTimestamp;

        double dif_lon = end.getLongitude() - start.getLongitude();
        double dif_lat = end.getLatitude() - start.getLatitude();

        double _lon = 0;
        double _lat = 0;

        double lon_tolerance = 0.0001;
        double lat_tolerance = 0.0001;

        Point pre = start;

        while (Math.abs(dif_lon + _lon) > lon_tolerance || Math.abs(dif_lat + _lat) > lat_tolerance) {

            while (lastTimestamp + timestamp_t >= System.currentTimeMillis()) {
                //自旋等待
            }
            lastTimestamp = System.currentTimeMillis();

            //经度是否已经到达指定误差内
            if (Math.abs(dif_lon + _lon) > lon_tolerance) {
                if (dif_lon > 0) {
                    _lon -= lon_t;
                } else if (dif_lon < 0) {
                    _lon += lon_t;
                }
            }

            //维度是否已经到达指定误差内
            if (Math.abs(dif_lat + _lat) > lat_tolerance) {
                if (dif_lat > 0) {
                    _lat -= lat_t;
                } else if (dif_lat < 0) {
                    _lat += lat_t;
                }
            }

            Point point = new Point();
            point.setLatitude(start.getLatitude() + _lat);
            point.setLongitude(start.getLongitude() + _lon);
            point.setTimestamp(System.currentTimeMillis());

            log.info("point = {}", point);

            double distance = EarthMapUtil.distance(pre.getLatitude() , pre.getLongitude() , point.getLatitude(), point.getLongitude());
            double sum = EarthMapUtil.distance(start.getLatitude() + _lat, start.getLongitude() + _lon, start.getLatitude(), start.getLongitude());
            pre = point;
            log.debug("distance = {},sum = {}", distance, sum);

            pilotingService.process(point);
        }

    }


}
