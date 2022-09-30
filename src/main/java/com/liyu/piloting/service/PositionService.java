package com.liyu.piloting.service;

import com.liyu.piloting.model.Point;
import com.liyu.piloting.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.joda.LocalDateTimeParser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author liyu
 * date 2022/9/18 19:27
 * description
 */
@Component
@Slf4j
public class PositionService {

    @Autowired
    PilotingService pilotingService;

    public void processMsg(String msg) {

        if (msg.startsWith("$GPRM")) {
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
            point.setLongitude(Double.parseDouble(longitude) / 100);
            point.setLatitude(Double.parseDouble(latitude) / 100);
            point.setSpeed(Double.parseDouble(speed));
            point.setTimestamp(TimeUtil.parseGPRMCTime(date, time));
            log.debug("processMsg $GPRM point={}", point.toString());
            pilotingService.process(point);
        }

//        else if (msg.startsWith("&GPGGA")) {
//            String[] split = msg.split(",");
//            String time = split[1];
//            String latitude = split[2];
//            String longitude = split[4];
////            log.debug("processMsg $GPGGA time={},latitude={},longitude={}", time, latitude, longitude);
//        } else if (msg.startsWith("$GPGLL")) {
//            String[] split = msg.split(",");
//            String latitude = split[1];
//            String longitude = split[3];
//            String time = split[5];
////            log.debug("processMsg $GPGGA time={},latitude={},longitude={}", time, latitude, longitude);
//        }
    }
}
