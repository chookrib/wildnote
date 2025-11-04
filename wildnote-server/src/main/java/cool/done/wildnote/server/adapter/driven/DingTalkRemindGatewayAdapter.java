package cool.done.wildnote.server.adapter.driven;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.application.ExtraLogService;
import cool.done.wildnote.server.domain.RemindGateway;
import cool.done.wildnote.server.utility.JsonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉提醒 Gateway 接口 Adapter
 */
@Component
public class DingTalkRemindGatewayAdapter implements RemindGateway {

    private static final Logger logger = LoggerFactory.getLogger(DingTalkRemindGatewayAdapter.class);

    @Value("${wildnote.remind-dingtalk-key:}")
    private String remindDingTalkKey;

    @Value("${wildnote.remind-dingtalk-secret:}")
    private String remindDingTalkSecret;

    @Value("${wildnote.remind-dingtalk-chatid:}")
    private String remindDingTalkChatId;

    private final ExtraLogService extraLogService;

    public DingTalkRemindGatewayAdapter(ExtraLogService extraLogService) {
        this.extraLogService = extraLogService;
    }

    @Override
    public void remind(String message) {
        try {
            String response = dingTalkChatSend(message);
            extraLogService.logRemindInfo(String.format("钉钉提醒结果: %s %s", message, response), logger);
        } catch (Exception ex) {
            extraLogService.logRemindError(String.format("钉钉提醒异常: %s", ex.getMessage()), logger);
        }
    }

    private static String DINGTALK_ACCESS_TOKEN = "";
    private static Date DINGTALK_ACCESS_TOKEN_EXPIRE_TIME = null;

    /**
     * 刷新钉钉AccessToken
     */
    private void dingTalkAccessTokenRefresh() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("appKey", remindDingTalkKey);
        requestBody.put("appSecret", remindDingTalkSecret);
        String responseBody = new RestTemplate().postForObject("https://api.dingtalk.com/v1.0/oauth2/accessToken",
                requestBody, String.class
        );
        //logger.info(responseBody);
        JsonNode responseJson = JsonUtility.deserialize(responseBody);
        String accessToken = responseJson.path("accessToken").asText();
        int expireIn = responseJson.path("expiresIn").asInt();
        DINGTALK_ACCESS_TOKEN = accessToken;
        DINGTALK_ACCESS_TOKEN_EXPIRE_TIME = new Date(System.currentTimeMillis() + (expireIn - 600) * 1000L);
    }

    /**
     * 发送钉钉群消息
     */
    private String dingTalkChatSend(String message) {
        if (DINGTALK_ACCESS_TOKEN_EXPIRE_TIME == null || DINGTALK_ACCESS_TOKEN_EXPIRE_TIME.before(new Date())) {
            dingTalkAccessTokenRefresh();
        }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatid", remindDingTalkChatId);
        Map<String, Object> requestBodyMsg = new HashMap<>();
        requestBodyMsg.put("msgtype", "text");
        Map<String, String> requestBodyMsgText = new HashMap<>();
        requestBodyMsgText.put("content", message);
        requestBodyMsg.put("text", requestBodyMsgText);
        requestBody.put("msg", requestBodyMsg);

        String url = String.format("https://oapi.dingtalk.com/chat/send?access_token=%s", DINGTALK_ACCESS_TOKEN);
        String responseBody = new RestTemplate().postForObject(url, requestBody, String.class);
        return responseBody;
    }
}
