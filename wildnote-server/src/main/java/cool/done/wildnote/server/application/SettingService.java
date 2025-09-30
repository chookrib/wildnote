package cool.done.wildnote.server.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cool.done.wildnote.server.utility.JsonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
        return JsonUtility.readTree(config);
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

    /**
     * 取收藏
     */
    public List<String> getFavorite() {
        JsonNode settingJson = getSettingJson();
        List<String> paths = new ArrayList<>();
        settingJson.path("favoriteNotePath").forEach(node -> paths.add(node.asText()));
        return paths;
    }

    /**
     * 存收藏
     */
    public void setFavorite(List<String> paths) {
        JsonNode settingJson = getSettingJson();
        ObjectNode objectNode = (ObjectNode) settingJson;
        objectNode.set("favoriteNotePath", objectNode.arrayNode().addAll(
                paths.stream().map(objectNode::textNode).collect(java.util.stream.Collectors.toList())
        ));
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(noteExploreService.combineAbsPath(settingPath).toUri()),
                    objectNode
            );
        } catch (IOException e) {
            throw new ApplicationException("保存配置文件异常: " + e.getMessage());
        }
    }
}
