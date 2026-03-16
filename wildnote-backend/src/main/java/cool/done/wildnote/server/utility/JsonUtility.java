package cool.done.wildnote.server.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Json Utility
 */
public class JsonUtility {

    /**
     * 反序列化 JSON 字符串为 JsonNode
     */
    public static JsonNode deserialize(String data) {
        try {
            return new ObjectMapper().readTree(data);
        } catch (JsonProcessingException ex) {
            throw new UtilityException("反序列化 JSON 异常", ex);
        }
    }

    /**
     * 序列化对象为 JSON 字符串
     */
    public static String serialize(Object data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new UtilityException("序列化 JSON 异常", ex);
        }
    }
}
