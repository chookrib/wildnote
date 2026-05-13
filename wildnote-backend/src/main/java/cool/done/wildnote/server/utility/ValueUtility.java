package cool.done.wildnote.server.utility;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * 值Utility
 */
public class ValueUtility {

    /**
     * 判断字符串是否为 null 或空字符串
     */
    public static boolean isEmptyString(String value) {
        return value == null || value.isBlank();    // isEmpty()
    }

    //==================================================================================================================

    /**
     * 转 bool，失败返回 null
     */
    public static Boolean toBoolOrNull(String value) {
        if (isEmptyString(value))
            return null;
        if (Arrays.asList("true", "1", "t", "y", "yes", "on").contains(value.trim().toLowerCase()))
            return true;
        else if (Arrays.asList("false", "0", "f", "n", "no", "off").contains(value.trim().toLowerCase()))
            return false;
        return null;
    }

    /**
     * 转 bool，失败返回默认值
     */
    public static boolean toBoolOrDefault(String value, boolean defaultValue) {
        Boolean b = toBoolOrNull(value);
        if (b != null)
            return b;
        return defaultValue;
    }

    //==================================================================================================================

    /**
     * 转 int，失败返回 null
     */
    public static Integer toIntOrNull(String value) {
        if (isEmptyString(value))
            return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 转 int，失败返回默认值
     */
    public static int toIntOrDefault(String value, int defaultValue) {
        Integer i = toIntOrNull(value);
        if (i != null)
            return i;
        return defaultValue;
    }

    //==================================================================================================================

    /**
     * 转 long，失败返回 null
     */
    public static Long toLongOrNull(String value) {
        if (isEmptyString(value))
            return null;
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 转 long，失败返回默认值
     */
    public static long toLongOrDefault(String value, long defaultValue) {
        Long l = toLongOrNull(value);
        if (l != null)
            return l;
        return defaultValue;
    }

    //==================================================================================================================

    /**
     * 转 decimal，失败返回 null
     */
    public static BigDecimal toDecimalOrNull(String value) {
        if (isEmptyString(value))
            return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 转 decimal，失败返回默认值
     */
    public static BigDecimal toDecimalOrDefault(String value, BigDecimal defaultValue) {
        BigDecimal d = toDecimalOrNull(value);
        if (d != null)
            return d;
        return defaultValue;
    }

    //==================================================================================================================
    // datetime 精度统一为毫秒

    /**
     * 获取当前时间，精度到毫秒
     */
    public static LocalDateTime getDateTimeNow() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    /**
     * 格式化 datetime，精度到秒
     */
    public static String formatDateTime(LocalDateTime value) {
        if (value != null)
            return value.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "";
    }

    /**
     * 格式化 datetime，精度到毫秒
     */
    public static String formatDateTimeMillisecond(LocalDateTime value) {
        if (value != null)
            return value.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        return "";
    }

    // 构建支持可选时间部分的日期时间格式化器
    private static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendPattern(" HH:mm")
            .optionalStart().appendPattern(":ss")
            // 支持 1 到 9 位小数秒
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd().optionalEnd().optionalEnd()
            // 如果只有日期，默认时间为 00:00:00.000
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter();

    /**
     * 转 datetime，精度到毫秒，失败返回 null
     */
    public static LocalDateTime toDateTimeOrNull(String value) {
        // try {
        //    return DateUtils.parseDate(value.trim(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd");
        //} catch (Exception ex) {
        //    return null;
        //}

        if (isEmptyString(value))
            return null;

        // DateUtils.parseDate(value.trim(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd");
        // LocalDateTime.parse(value.trim(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // try {
        //     return LocalDateTime.parse(value.trim().replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        // } catch (DateTimeParseException ex) {
        //     try {
        //         // 如果只有日期，返回日期 00:00:00
        //         return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        //     } catch (DateTimeParseException exc) {
        //         return null;
        //     }
        // }

        String dt = value.trim();

        // 防御性处理：防止输入超过 9 位小数导致解析失败
        int dotIndex = dt.indexOf('.');
        if (dotIndex > 0 && dt.length() - dotIndex - 1 > 9) {
            dt = dt.substring(0, dotIndex + 10);
        }

        try {
            return LocalDateTime.parse(dt, DATETIME_FORMATTER).truncatedTo(ChronoUnit.MILLIS);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * 转 datetime，精度到毫秒，失败返回默认值
     */
    public static LocalDateTime toDateTimeOrDefault(String value, LocalDateTime defaultValue) {
        LocalDateTime dt = toDateTimeOrNull(value);
        if (dt != null)
            return dt;
        if (defaultValue == null)
            return null;
        return defaultValue.truncatedTo(ChronoUnit.MILLIS);
    }

    //==================================================================================================================

    /**
     * 格式化 date
     */
    public static String formatDate(LocalDate value) {
        if (value != null)
            return value.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "";
    }

    /**
     * 转 date，失败返回 null
     */
    public static LocalDate toDateOrNull(String value) {
        if (isEmptyString(value))
            return null;

        //  LocalDate.parse(value.trim(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // try {
        //     return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        // } catch (DateTimeParseException ex) {
        //     // 如果是带时间的日期时间，提取日期部分
        //     try {
        //         return LocalDateTime.parse(
        //                 value.trim().replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME
        //         ).toLocalDate();
        //     } catch (DateTimeParseException exc) {
        //         return null;
        //     }
        // }

        String d = value.trim();
        if (d.length() < 10)
            return null;

        try {
            return LocalDate.parse(d.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 转 date，失败返回默认值
     */
    public static LocalDate toDateOrDefault(String value, LocalDate defaultValue) {
        LocalDate d = toDateOrNull(value);
        if (d != null)
            return d;
        return defaultValue;
    }

    //==================================================================================================================
    // time 精度统一为秒

    /**
     * 格式化 time，精度到秒
     */
    public static String formatTime(LocalTime value) {
        if (value != null)
            return value.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        return "";
    }

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            // 将 "日期 + 空格" 作为一个独立的可选整体
            .optionalStart()
            .appendPattern("yyyy-MM-dd")
            .appendPattern(" ")
            .optionalEnd()
            // 时间部分必选
            .appendPattern("HH:mm")
            // 秒可选
            .optionalStart()
            .appendPattern(":ss")
            // 纳秒（小数位）可选，且依赖于秒的存在
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .optionalEnd()
            .toFormatter();

    /**
     * 转 time，精度到秒，失败返回 null
     */
    public static LocalTime toTimeOrNull(String value) {
        if (isEmptyString(value))
            return null;

        // try {
        //     // return LocalTime.parse(value.trim(), java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        //     return LocalTime.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_TIME);
        // } catch (DateTimeParseException ex) {
        //     try {
        //         return LocalDateTime.parse(
        //                 value.trim().replace(" ", "T"), DateTimeFormatter.ISO_LOCAL_DATE_TIME
        //         ).toLocalTime();
        //     } catch (DateTimeParseException exc) {
        //         return null;
        //     }
        // }

        String t = value.trim();

        // 防御性处理：防止输入超过 9 位小数导致解析失败
        int dotIndex = t.indexOf('.');
        if (dotIndex > 0 && t.length() - dotIndex - 1 > 9) {
            t = t.substring(0, dotIndex + 10);
        }

        try {
            return LocalTime.parse(t, TIME_FORMATTER).truncatedTo(ChronoUnit.SECONDS);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * 转 time，精度到秒，失败返回默认值
     */
    public static LocalTime toTimeOrDefault(String value, LocalTime defaultValue) {
        LocalTime t = toTimeOrNull(value);
        if (t != null)
            return t;
        if (defaultValue == null)
            return null;
        return defaultValue.truncatedTo(ChronoUnit.SECONDS);
    }

    // =================================================================================================================

    /**
     * Long 毫秒转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Long millisTimestamp) {
        if (millisTimestamp == null) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(
                millisTimestamp / 1000, 0, ZoneOffset.ofHours(8)
        );
    }

    /**
     * 计算等待时长，单位毫秒
     */
    public static Long calcDelayTime(Long millisTimestamp) {
        if (millisTimestamp == null) {
            return null;
        }
        // return millisTimestamp - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000;
        return millisTimestamp - LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)) * 1000;
    }
}
