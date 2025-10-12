package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.SettingService;
import cool.done.wildnote.server.domain.RemindGateway;
import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Webhook Controller
 */
@RestController
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private final ApplicationContext applicationContext;
    private final NoteExploreService noteExploreService;
    private final SettingService settingService;

    public WebhookController(
            ApplicationContext applicationContext,
            SettingService settingService,
            NoteExploreService noteExploreService) {
        this.applicationContext = applicationContext;
        this.settingService = settingService;
        this.noteExploreService = noteExploreService;
    }

    /**
     * 提醒 Webhook
     */
    @RequestMapping(value = "/webhook/remind/{name}", method = RequestMethod.GET)
    public Result remind(@PathVariable String name, @RequestParam String message) {
        if (ValueUtility.isBlank(name) || ValueUtility.isBlank(message))
            throw new ControllerException("Webhook remind 参数错误");

        String value = settingService.getRemindWebhook(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook remind 未配置"));

        // RemindGateway remindGateway = applicationContext.getBean(RemindGateway.class);
        RemindGateway remindGateway = (RemindGateway) applicationContext.getBean(value);
        remindGateway.remind(String.format("Webhook remind 成功: %s %s", name, message));

        return Result.ok();
    }

    /**
     * 记录 Webhook
     */
    @RequestMapping(value = "/webhook/record/{name}", method = RequestMethod.GET)
    public Result record(@PathVariable String name, @RequestParam String content) {
        if (ValueUtility.isBlank(name) || ValueUtility.isBlank(content))
            throw new ControllerException("Webhook record 参数错误");

        String value = settingService.getRecordWebhook(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook record 未配置"));

        Path notePath = noteExploreService.combineAbsPath(value);

        //File noteFile = new File(notePath);
        //if (!noteFile.exists())
        //    throw new ControllerException(String.format("配置的记录路径不存在"));

        // 写入文件末尾
        //try (FileWriter writer = new FileWriter(notePath.toFile(), StandardCharsets.UTF_8, true)) {
        //    writer.write(String.format(
        //            "\n\n%s %s",
        //            new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()),
        //            content)
        //    );
        //} catch (IOException e) {
        //    throw new ControllerException(String.format("Webhook record 异常: %s", e.getMessage()));
        //}

        // 写入文件开头
        try {
            String oldContent = "";
            if (notePath.toFile().exists()) {
                oldContent = java.nio.file.Files.readString(notePath, StandardCharsets.UTF_8);
            }
            try (FileWriter writer = new FileWriter(notePath.toFile(), StandardCharsets.UTF_8, false)) {
                writer.write(String.format(
                        "%s %s\n\n%s",
                        new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()),
                        content,
                        oldContent.trim())
                );
            }
        } catch (IOException e) {
            throw new ControllerException(String.format("Webhook record 异常: %s", e.getMessage()));
        }

        return Result.ok();
    }
}
