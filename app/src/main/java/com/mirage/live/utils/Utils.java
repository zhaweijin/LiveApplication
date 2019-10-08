package com.mirage.live.utils;

import android.util.Log;

import java.util.Formatter;
import java.util.Locale;

public class Utils {


    private static final StringBuilder stringBuilder = new StringBuilder();
    private static final Formatter stringFormatter = new Formatter(stringBuilder, Locale.getDefault());
    /**
     * 打印测试
     * @param tag
     * @param value
     */
    public static void print(String tag,String value){
       Log.v(tag,value);
    }


    public static String getTimeString(int milliSeconds) {
        long seconds = (milliSeconds % 60000L) / 1000L;
        long minutes = (milliSeconds % 3600000L) / 60000L;
        long hours = (milliSeconds % 86400000L) / 3600000L;
        long days = (milliSeconds % (86400000L * 7L)) / 86400000L;

        stringBuilder.setLength(0);
        return days > 0 ? stringFormatter.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds).toString()
                : hours > 0 ? stringFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : stringFormatter.format("%02d:%02d", minutes, seconds).toString();
    }
}
