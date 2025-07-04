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
 * 站点配置Service
 */
@Service
public class SiteConfigService {
    private static final Logger logger = LoggerFactory.getLogger(SiteConfigService.class);

    private final String siteConfigPath;
    private final NoteService noteService;

    public SiteConfigService(@Value("${wildnote.site-config-path:}") String siteConfigPath, NoteService noteService) {
        this.siteConfigPath = siteConfigPath;
        this.noteService = noteService;
    }

    /**
     * 获取配置JSON
     */
    private JsonNode getConfigJson(){
        String config = "";
        try {
            config = Files.readString(noteService.combineAbsPath(siteConfigPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ValidationException(String.format("读取 %s 失败: %s", siteConfigPath, e.getMessage()));
        }
        return JacksonUtility.readTree(config);
    }

    /**
     * 认证
     */
    public boolean auth(String username, String password) {
        JsonNode configJson = getConfigJson();
        String configUsername = configJson.path("auth").path("username").asText();
        String configPassword = configJson.path("auth").path("password").asText();
        return configUsername.equals(username) && configPassword.equals(password);
    }

    /**
     * 获取提醒钩子名称
     */
    public String getHookRemindName() {
        JsonNode configJson = getConfigJson();
        return configJson.path("hook").path("remind").asText();
    }

    /**
     * 获取记录钩子名称
     */
    public String getHookRecordNote(String name) {
        JsonNode configJson = getConfigJson();
        return configJson.path("hook").path("record").path(name).asText();
    }
}
