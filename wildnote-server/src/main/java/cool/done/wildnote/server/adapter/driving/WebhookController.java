package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.domain.RemindGateway;
import cool.done.wildnote.server.utility.ValueUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
            NoteExploreService noteExploreService
    ) {
        this.applicationContext = applicationContext;
        this.noteSettingService = noteSettingService;
        this.noteExploreService = noteExploreService;
    }

    /**
     * 提醒 Webhook
     */
    @RequestMapping(value = "/webhook/remind/{name}", method = RequestMethod.GET)
    public Result remind(HttpServletRequest request, @PathVariable String name) {
        if (ValueUtility.isBlank(name))
            throw new ControllerException("Webhook remind name 参数错误");

        name = name.trim();
        String message = RequestValueHelper.getRequestParamStringTrimReq(request, "message");

        String value = noteSettingService.getRemindWebhook(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook remind 未配置 %s", name));

        // RemindGateway remindGateway = applicationContext.getBean(RemindGateway.class);
        RemindGateway remindGateway = (RemindGateway) applicationContext.getBean(value);
        remindGateway.remind(String.format("Webhook remind 成功: %s %s", name, message));

        return Result.ok();
    }

    /**
     * 记录 Webhook，未指定 mode 默认为 append
     */
    @RequestMapping(value = "/webhook/record/{name}", method = RequestMethod.GET)
    public Result record(HttpServletRequest request, @PathVariable String name) {
        if (ValueUtility.isBlank(name))
            throw new ControllerException("Webhook record name 参数错误");

        name = name.trim();
        String mode = RequestValueHelper.getRequestParamStringTrimOrDefault(request, "append", "mode");
        String content = RequestValueHelper.getRequestParamStringTrimReq(request, "content");

        String value = noteSettingService.getRecordWebhook(name);
        if (ValueUtility.isBlank(value))
            throw new ControllerException(String.format("Webhook record 未配置 %s", name));

        String contentWithTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()) + " " + content;

        if ("append".equalsIgnoreCase(mode)) {
            noteExploreService.appendFileContent(value, "\n\n" + contentWithTime);
        } else if ("insert".equalsIgnoreCase(mode)) {
            noteExploreService.insertFileContent(value, "\n\n" + contentWithTime);
        } else {
            throw new ControllerException("Webhook record mode 非法");
        }

        return Result.okData(Map.of(
                "content", contentWithTime,
                "mode", mode
        ));
    }
}
