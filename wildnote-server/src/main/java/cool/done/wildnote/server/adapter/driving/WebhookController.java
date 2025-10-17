package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteSettingService;
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
    private final NoteSettingService noteSettingService;

    public WebhookController(
            ApplicationContext applicationContext,
            NoteSettingService noteSettingService,
            NoteExploreService noteExploreService) {
        this.applicationContext = applicationContext;
        this.noteSettingService = noteSettingService;
        this.noteExploreService = noteExploreService;
    }

    /**
     * 提醒 Webhook
     */
    @RequestMapping(value = "/webhook/remind/{name}", method = RequestMethod.GET)
    public Result remind(@PathVariable String name, @RequestParam String message) {
        if (ValueUtility.isBlank(name) || ValueUtility.isBlank(message))
            throw new ControllerException("Webhook remind 参数错误");

        String value = noteSettingService.getRemindWebhook(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook remind 未配置"));

        // RemindGateway remindGateway = applicationContext.getBean(RemindGateway.class);
        RemindGateway remindGateway = (RemindGateway) applicationContext.getBean(value);
        remindGateway.remind(String.format("Webhook remind 成功: %s %s", name, message));

        return Result.ok();
    }

    /**
     * 记录 Webhook，未指定 mode 默认为 append
     */
    @RequestMapping(value = "/webhook/record/{name}", method = RequestMethod.GET)
    public Result record(@PathVariable String name,
                         @RequestParam(value = "mode", defaultValue = "append") String mode,
                         @RequestParam String content) {
        if (ValueUtility.isBlank(name) || ValueUtility.isBlank(content))
            throw new ControllerException("Webhook record 参数错误");

        String value = noteSettingService.getRecordWebhook(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException("Webhook record 未配置");

        String newContent = String.format(
                "\n\n%s %s",
                new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()),
                content);

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(value, newContent);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(value, newContent);
        } else {
            throw new ControllerException("Webhook record mode 非法");
        }

        return Result.ok();
    }
}
