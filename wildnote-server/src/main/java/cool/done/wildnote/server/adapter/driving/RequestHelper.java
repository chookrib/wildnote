package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.utility.JsonUtility;

/**
 * 请求 Helper
 */
public class RequestHelper {
    /**
     * 获取请求体JSON内容
     */
    public static JsonNode toJson(String requestBody) {
        try {
            return JsonUtility.deserialize(requestBody);
        }
        catch (Exception e) {
            throw new ControllerException("请求体不是合法的JSON格式");
        }
    }
}
