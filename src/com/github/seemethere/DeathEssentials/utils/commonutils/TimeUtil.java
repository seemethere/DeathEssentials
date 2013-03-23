package com.github.seemethere.DeathEssentials.utils.commonutils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    public static String StringTime(Long millis) {
        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        int day = hour * 24;
        StringBuilder sb = new StringBuilder();
        millis = writeTime(sb, millis, day, "days");
        millis = writeTime(sb, millis, hour, "hours");
        millis = writeTime(sb, millis, minute, "minutes");
        writeTime(sb, millis, second, "seconds");
        return sb.toString();
    }

    private static long writeTime(StringBuilder sb, Long millis, int unit, String unitString) {
        if (millis > unit) {
            sb.append(millis / unit).append(" ").append(unitString).append(" ");
            millis %= unit;
        }
        return millis;
    }

    public static long ParseTime(String time) {
        Pattern timePattern = Pattern.compile(
                "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
                        "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" +
                        "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
                        "(?:([0-9]+)\\s*(?:s[a-z]*)?)?",
                Pattern.CASE_INSENSITIVE);
        Matcher m = timePattern.matcher(time);
        Long millis = (long) 0;
        if (m.find()) {
            millis += toTimeUnit(TimeUnit.DAYS, m, 1);
            millis += toTimeUnit(TimeUnit.HOURS, m, 2);
            millis += toTimeUnit(TimeUnit.MINUTES, m, 3);
            millis += toTimeUnit(TimeUnit.SECONDS, m, 4);
        }
        return millis;
    }

    private static long toTimeUnit(TimeUnit timeUnit, Matcher m, int group) {
        if (m.group(group) != null && !m.group(group).isEmpty())
            return timeUnit.toMillis(Integer.parseInt(m.group(group)));
        return 0;
    }
}
