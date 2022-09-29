package com.liyu.piloting.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liyu
 * date 2020/10/15 11:48
 * description
 */
@Slf4j(topic = "EarthMapUtil")
@UtilityClass
public class EarthMapUtil {
    private final static double EARTH_RADIUS = 6378.137f;

    public double distance(double lat1, double lng1, double lat2, double lng2) {
        double x1 = Math.cos(lat1) * Math.cos(lng1);
        double y1 = Math.cos(lat1) * Math.sin(lng1);
        double z1 = Math.sin(lat1);
        double x2 = Math.cos(lat2) * Math.cos(lng2);
        double y2 = Math.cos(lat2) * Math.sin(lng2);
        double z2 = Math.sin(lat2);
        double lineDistance =
                Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
        double realDistance = EARTH_RADIUS * Math.PI * 2 * Math.asin(0.5 * lineDistance) / 180;
        return realDistance * 1000;

    }


    /**
     * 谷歌地图的计算公式
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = getRadian(lat1);
        double radLat2 = getRadian(lat2);
        double a = radLat1 - radLat2;// 两点纬度差
        double b = getRadian(lng1) - getRadian(lng2);// 两点的经度差
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1)
                * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s * 1000;
    }

    /**
     * 角度弧度计算公式 rad:(). <br/>
     *
     * 360度=2π π=Math.PI
     *
     * x度 = x*π/360 弧度
     *
     * @author chiwei
     * @param degree
     * @return
     * @since JDK 1.6
     */
    private static double getRadian(double degree) {
        return degree * Math.PI / 180.0;
    }

    public static void main(String[] args) {
        double distance = distance(113.962, 22.749, 114.062, 22.597);
        System.out.println("distance = " + distance);
        double distance2= distance( 114.062, 22.597,113.962, 22.749);
        System.out.println("distance2 = " + distance2);
    }
}
