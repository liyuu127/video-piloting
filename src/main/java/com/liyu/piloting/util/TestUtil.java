package com.liyu.piloting.util;

import com.liyu.piloting.model.Point;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
public class TestUtil {
    public static Point extrcPoint(String msg) {
        Point point = new Point();
        String[] split = msg.split(",");
        String time = split[1];
        String latitude = split[3];
        String longitude = split[5];
        String speed = split[7];
        String date = split[9];
        if (StringUtils.isAnyBlank(time, latitude, longitude, speed, date)) {
            log.error("msg null");
            log.debug("processMsg $GPRM time={},latitude={},longitude={},speed={},date={}", time, latitude, longitude, speed, date);
        }
        point.setLongitude(NMEA0183Util.convertLonDegree(longitude));
        point.setLatitude(NMEA0183Util.convertLaDegree(latitude));
        point.setSpeed(Double.parseDouble(speed));
        point.setTimestamp(TimeUtil.parseGPRMCTime(date, time));
        point.setDataTime(LocalDateTime.ofEpochSecond(point.getTimestamp() / 1000, 0, ZoneOffset.of("+8")));
        log.debug("processMsg $GPRM point={}", point);
        return point;
    }

    public static Double getDistance(Point p1, Point p2) {
        return EarthMapUtil.distance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
    }
}
