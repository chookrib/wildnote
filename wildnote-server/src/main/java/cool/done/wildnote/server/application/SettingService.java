package cool.done.wildnote.server.application;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.utility.JacksonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 配置 Service
 */
@Component
public class SettingService {

    private static final Logger logger = LoggerFactory.getLogger(SettingService.class);

    private final String settingPath;
    private final NoteExploreService noteExploreService;

    public SettingService(@Value("${wildnote.setting-path:}") String settingPath, NoteExploreService noteExploreService) {
        this.settingPath = settingPath;
        this.noteExploreService = noteExploreService;
    }

    /**
     * 获取配置文件 JSON 内容
     */
    public JsonNode getSettingJson() {
        String config = "";
        try {
            config = Files.readString(noteExploreService.combineAbsPath(settingPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApplicationException(String.format("读取配置文件 %s 异常: %s", settingPath, e.getMessage()));
        }
        return JacksonUtility.readTree(config);
    }

    ///**
    // * 认证
    // */
    //public boolean auth(String username, String password) {
    //    JsonNode settingJson = getSettingJson();
    //    String u = settingJson.path("auth").path("username").asText();
    //    String p = settingJson.path("auth").path("password").asText();
    //    return u.equals(username) && p.equals(password);
    //}

    /**
     * 获取提醒 Webhook 配置
     */
    public String getRemindWebhook(String name) {
        JsonNode settingJson = getSettingJson();
        return settingJson.path("webhook").path("remind").path(name).asText();
    }

    /**
     * 获取记录 Webhook 配置
     */
    public String getRecordWebhook(String name) {
        JsonNode settingJson = getSettingJson();
        return settingJson.path("webhook").path("record").path(name).asText();
    }
}
