package cool.done.wildnote.server.application;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cool.done.wildnote.server.utility.JsonUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔记配置 Service
 */
@Component
public class NoteSettingService {

    private static final Logger logger = LoggerFactory.getLogger(NoteSettingService.class);

    private String settingFileAbsPath = "";
    private String settingContent = "";
    private JsonNode settingContentJson = new ObjectMapper().createObjectNode();
    private final ExtraLogService extraLogService;


    public NoteSettingService(ExtraLogService extraLogService) {
        this.extraLogService = extraLogService;
    }

    /**
     * 初始化
     */
    public void loadSetting(String settingFileAbsPath) {
        this.settingFileAbsPath = settingFileAbsPath;
        try {
            this.settingContent = Files.readString(Path.of(this.settingFileAbsPath), StandardCharsets.UTF_8);
            this.settingContentJson = JsonUtility.deserialize(this.settingContent);
            extraLogService.logNoteInfo(
                    String.format("读取笔记配置文件 %s 成功", settingFileAbsPath), logger
            );
        } catch (IOException ex) {
            extraLogService.logNoteError(
                    String.format("读取笔记配置文件 %s 异常，未更新配置: %s", settingFileAbsPath, ex.getMessage()), logger
            );
        }
    }

    /**
     * 获取笔记配置文件内容
     */
    public String getSettingContent() {
        return this.settingContent;
    }

    /**
     * 取认证密码
     */
    public String getAuthPassword(String username) {
        return this.settingContentJson.path("auth").path(username).asText();
    }

    /**
     * 获取提醒 Webhook 配置
     */
    public String getRemindWebhook(String name) {
        return this.settingContentJson.path("webhook").path("remind").path(name).asText();
    }

    /**
     * 获取记录 Webhook 配置
     */
    public String getRecordWebhook(String name) {
        return this.settingContentJson.path("webhook").path("record").path(name).asText();
    }

    /**
     * 取收藏
     */
    public List<String> getFavorite() {
        List<String> paths = new ArrayList<>();
        this.settingContentJson.path("favoriteNotePath").forEach(node -> paths.add(node.asText()));
        return paths;
    }

    /**
     * 存收藏
     */
    public void setFavorite(List<String> paths) {
        if (ValueUtility.isBlank(this.settingFileAbsPath))
            throw new ApplicationException("配置文件路径未初始化，无法保存收藏");

        ObjectNode objectNode = (ObjectNode) this.settingContentJson;
        objectNode.set("favoriteNotePath", objectNode.arrayNode().addAll(
                paths.stream().map(objectNode::textNode).collect(java.util.stream.Collectors.toList())
        ));
        ObjectMapper mapper = new ObjectMapper();
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        // 数组分行显示
        prettyPrinter.indentArraysWith(new DefaultIndenter("  ", DefaultIndenter.SYS_LF));
        try {
            //mapper.writerWithDefaultPrettyPrinter().writeValue(
            //        new File(this.settingFileAbsPath), objectNode
            //);
            mapper.writer(prettyPrinter).writeValue(
                    new File(this.settingFileAbsPath), objectNode
            );
        } catch (IOException ex) {
            throw new ApplicationException(String.format("保存配置文件异常: %s", ex.getMessage()), ex);
        }
    }
}
