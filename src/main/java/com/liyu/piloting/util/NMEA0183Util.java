package com.liyu.piloting.util;

/**
 * @author liyu
 * date 2022/10/8 12:16
 * description
 * NMEA语句中的经纬度信息为NMEA0183经纬度格式输出，而部分后台地图通常使用以“度”“分”的经纬度坐标来实现定位标注。所以 NMEA语句中的经纬度信息需要经过换算才能得到地图中的经纬度坐标，如果把经纬度信息
 * （2238.5260,N, 11401.9686,E）转换格式统一单位为度的形式（保留 6位小数），
 * 步骤如下：
 * 1. N（北纬） 2238.5260
 * （ 1） 2238.5260÷100=22.385260（取整） =22
 * （ 2） 385260÷60=6421
 * 得到以度形式的纬度坐标为 N 22.642100°
 * 2. E（东经） 11401.9686
 * （ 1） 11401.9686÷100=114.019686（取整） =114
 * （ 2） 019686÷60=0328.1
 * 得到以度形式的经度坐标为 E 114.032810°
 */
public class NMEA0183Util {


    public static double convertLaDegree(String la) {

        String las = la.replace(".", "").substring(2);
        int i = 0;
        while (i < las.length() && las.charAt(i) == '0') {
            i++;
        }
        String s = String.valueOf(Double.parseDouble(las) / 60);
        s = s.replace(".", "");
        String s1 = String.valueOf((int) (Double.parseDouble(la) / 100)) + "." + las.substring(0, i) + s;
        return Double.parseDouble(s1);
    }

    public static double convertLonDegree(String la) {

        String las = la.replace(".", "").substring(3);
        int i = 0;
        while (i < las.length() && las.charAt(i) == '0') {
            i++;
        }
        String s = String.valueOf(Double.parseDouble(las) / 60);
        s = s.replace(".", "");
        String s1 = String.valueOf((int) (Double.parseDouble(la) / 100)) + "." + las.substring(0, i) + s;
        return Double.parseDouble(s1);
    }

    public static void main(String[] args) {
        double v = convertLaDegree("2238.5260");
        System.out.println("v = " + v);
        double v1 = convertLonDegree("11401.9686");
        System.out.println("v1 = " + v1);
    }
}
