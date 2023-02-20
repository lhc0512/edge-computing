package cn.edu.scut.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static String getFlag() {
        var dateTime = LocalDateTime.now();
        var dateTime2 = dateTime.plusHours(8);
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh_mm_ss");
        return formatter.format(dateTime2);
    }
}
