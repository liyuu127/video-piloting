package com.liyu.piloting.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author liyu
 * date 2022/9/19 11:30
 * description
 */
public class TimeUtil {

    final static DateTimeFormatter ddMMyyHHmmss = DateTimeFormatter.ofPattern("ddMMyyHHmmss");
    public static long parseGPRMCTime(String date, String time) {
        String dateTimeStr = date + time.substring(0, time.indexOf("."));
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(dateTimeStr, ddMMyyHHmmss));
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

//    public static void main(String[] args) {
//        long l = parseGPRMCTime("190922", "025239.00");
//        System.out.println("l = " + l);
//        long l1 = System.currentTimeMillis();
//        System.out.println("l1 = " + l1);
//    }
}

