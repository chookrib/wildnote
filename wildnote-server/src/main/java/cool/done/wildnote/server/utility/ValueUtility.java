package cool.done.wildnote.server.utility;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 值Utility
 */
public class ValueUtility {

    /**
     * 判断字符串是否为 null 或空字符串
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();    // isEmpty()
    }

    // =================================================================================================================

    /**
     * Long 转 LocalDateTime
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
     * 计算等待时间
     */
    public static Long calcDelayTime(Long millisTimestamp) {
        if (millisTimestamp == null) {
            return null;
        }
        long delayTime = millisTimestamp - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000;
        //if(delayTime < 0) {
        //    return null;
        //}
        return delayTime;
    }
}
