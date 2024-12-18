package cool.done.wildnotesvc.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Jackson工具类
 */
public class JacksonUtils {

    /**
     * 读取JsonNode
     */
    public static JsonNode readTree(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
