package cool.done.wildnote.server.adapter.driven;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.domain.IRemindHandler;
import cool.done.wildnote.server.utility.JacksonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 提醒处理器
 */
@Component
public class RemindHandler implements IRemindHandler {
    private static final Logger logger = LoggerFactory.getLogger(RemindHandler.class);

    @Value("${wildnote.remind-url:}")
    private String remindUrl;

    private final String remindLogPath;

    @Value("${wildnote.remind-dingtalk-key:}")
    private String remindDingtalkKey;

    @Value("${wildnote.remind-dingtalk-secret:}")
    private String remindDingtalkSecret;

    @Value("${wildnote.remind-dingtalk-chatid:}")
    private String remindDingtalkChatId;

    public RemindHandler(@Value("${wildnote.remind-log-path:}") String remindLogPath) {
        this.remindLogPath = remindLogPath;
        if (!remindLogPath.isEmpty()) {
            File remindLogDir = new File(remindLogPath).getParentFile();
            if (remindLogDir != null && !remindLogDir.exists()) {
                remindLogDir.mkdirs();
            }
        }
    }

    @Override
    public void remind(String message) {

        //String log = "";
        //try {
        //    String url = remindUrl + message;
        //    new RestTemplate().getForObject(url, String.class);
        //    //logger.info("笔记提醒成功: {}", message);
        //    log = String.format("笔记提醒成功: %s", message);
        //}
        //catch (Exception e) {
        //    //logger.error("笔记提醒失败: {}", e.getMessage());
        //    log = String.format("笔记提醒失败: %s", e.getMessage());
        //}
        //
        //try {
        //    FileWriter fileWriter = new FileWriter(remindLogPath, StandardCharsets.UTF_8, true);
        //    fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
        //    fileWriter.close();
        //}
        //catch (Exception e) {
        //    logger.error("笔记提醒写日志失败: {} {}", log, e.getMessage());
        //}

        String log = "";
        try {
            String response = dingTalkChatSend(message);
            //logger.info("笔记提醒成功: {}", message);
            log = String.format("笔记提醒结果: %s %s", message, response);
        }
        catch (Exception e) {
            //logger.error("笔记提醒失败: {}", e.getMessage());
            log = String.format("笔记提醒失败: %s", e.getMessage());
        }

        try {
            FileWriter fileWriter = new FileWriter(remindLogPath, StandardCharsets.UTF_8, true);
            fileWriter.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + log + "\n");
            fileWriter.close();
        }
        catch (Exception e) {
            logger.error("笔记提醒写日志失败: {} {}", log, e.getMessage());
        }
    }

    private static String DING_TALK_ACCESS_TOKEN = "";
    private static Date DING_TALK_ACCESS_TOKEN_EXPIRE_TIME = null;

    /**
     * 刷新钉钉AccessToken
     */
    private void dingTalkAccessTokenRefresh() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("appKey", remindDingtalkKey);
        requestBody.put("appSecret", remindDingtalkSecret);
        String responseBody = new RestTemplate().postForObject("https://api.dingtalk.com/v1.0/oauth2/accessToken",
                requestBody, String.class
        );
        //logger.info(responseBody);
        JsonNode responseJson = JacksonUtility.readTree(responseBody);
        String accessToken = responseJson.path("accessToken").asText();
        int expireIn = responseJson.path("expiresIn").asInt();
        DING_TALK_ACCESS_TOKEN = accessToken;
        DING_TALK_ACCESS_TOKEN_EXPIRE_TIME = new Date(System.currentTimeMillis() + (expireIn - 600) * 1000L);
    }

    /**
     * 发送钉钉群消息
     */
    private String dingTalkChatSend(String message){
        if (DING_TALK_ACCESS_TOKEN_EXPIRE_TIME == null || DING_TALK_ACCESS_TOKEN_EXPIRE_TIME.before(new Date())) {
            dingTalkAccessTokenRefresh();
        }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chatid", remindDingtalkChatId);
        Map<String, Object> requestBodyMsg = new HashMap<>();
        requestBodyMsg.put("msgtype", "text");
        Map<String, String> requestBodyMsgText = new HashMap<>();
        requestBodyMsgText.put("content", message);
        requestBodyMsg.put("text", requestBodyMsgText);
        requestBody.put("msg", requestBodyMsg);

        String url = String.format("https://oapi.dingtalk.com/chat/send?access_token=%s", DING_TALK_ACCESS_TOKEN);
        String responseBody = new RestTemplate().postForObject(url, requestBody, String.class);
        return responseBody;
    }
}
