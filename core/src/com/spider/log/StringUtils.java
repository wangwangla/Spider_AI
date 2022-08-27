package com.spider.log;

import org.junit.Test;

import java.util.Locale;

public class StringUtils {
    @Test
    public void testFormat() {
        System.out.println(StringUtils.format(
                "Can't create cache dir %s, won't be able to log to a file", "x"));
    }

    public static String format(String fmt, Object... args) {
        return String.format(Locale.getDefault(), fmt, args);
    }

    @Test
    public void testFormatTime() {
        System.out.println(formatRaceTime(1000.7744F));
    }

    public static String formatRaceTime(float time) {
        int minutes = (int) (time / 60);
        int seconds = (int) (time) % 60;
        int millis = (int) (time * 1000) % 1000;
        return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis);
//        return String.format(Locale.US, "%02d.%03d",  seconds, millis);
    }

    @Test
    public void testFormatRankInTable() {
        System.out.println(formatRankInTable(1000));
    }

    public static String formatRankInTable(int rank) {
        return format("%d.", rank);
    }

//    private static DefaultImplementation defaultImplementation = new DefaultImplementation();
    @Test
    public void testFormatRankInHud(){
//        System.out.println(formatRankInHud(1));
    }

//    public static String formatRankInHud(int rank) {
//        switch (rank) {
//            case 1:
//                return defaultImplementation.trc("1st",null);
//            case 2:
//                return defaultImplementation.trc("2nd",null);
//            case 3:
//                return defaultImplementation.trc("3rd",null);
//            case 4:
//                return defaultImplementation.trc("4th",null);
//            case 5:
//                return defaultImplementation.trc("5th",null);
//            case 6:
//                return defaultImplementation.trc("6th",null);
//            default:
//                return String.valueOf(rank);
//        }
//    }
}
