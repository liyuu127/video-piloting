package com.liyu.piloting.util;

/**
 * @author liyu
 * date 2022/10/8 12:16
 * description
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
        double v = convertLaDegree("2152.52517");
        System.out.println("v = " + v);
        double v1 = convertLonDegree("11152.27741");
        System.out.println("v1 = " + v1);
    }
}
