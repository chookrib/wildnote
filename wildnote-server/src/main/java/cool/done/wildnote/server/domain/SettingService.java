package cool.done.wildnote.server.domain;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.utility.JacksonUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 设置Service
 */
@Service
public class SettingService {
    private static final Logger logger = LoggerFactory.getLogger(SettingService.class);

    private final String settingPath;
    private final NoteService noteService;

    public SettingService(@Value("${wildnote.setting-path:}") String settingPath, NoteService noteService) {
        this.settingPath = settingPath;
        this.noteService = noteService;
    }

    /**
     * 获取设置JSON
     */
    public JsonNode getSettingJson(){
        String config = "";
        try {
            config = Files.readString(noteService.combineAbsPath(settingPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ValidationException(String.format("读取配置文件 %s 异常", settingPath), e);
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
     * 获取提醒钩子名称
     */
    public String getHookRemindName() {
        JsonNode settingJson = getSettingJson();
        return settingJson.path("hook").path("remind").asText();
    }

    /**
     * 获取记录钩子名称
     */
    public String getHookRecordNote(String name) {
        JsonNode settingJson = getSettingJson();
        return settingJson.path("hook").path("record").path(name).asText();
    }
}
