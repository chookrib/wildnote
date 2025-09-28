package cool.done.wildnote.server.utility;

/**
 * 值Utility
 */
public class ValueUtility {

    /**
     * 判断字符串是否null或空字符串
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();    // isEmpty()
    }
}
