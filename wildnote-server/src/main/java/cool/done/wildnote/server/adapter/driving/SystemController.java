package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.domain.*;
import cool.done.wildnote.server.utility.ValueUtility;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统 Controller
 */
@RestController
public class SystemController {

    private final RemindGateway remindGateway;
    private final SmsGateway smsGateway;
    private final NoteSettingService noteSettingService;

    public SystemController(RemindGateway remindGateway, SmsGateway smsGateway,
                            NoteSettingService noteSettingService) {
        this.remindGateway = remindGateway;
        this.smsGateway = smsGateway;
        this.noteSettingService = noteSettingService;
    }

    /**
     * 取配置
     */
    @RequestMapping(value = "/api/system/setting", method = RequestMethod.GET)
    public Result setting() {
        return Result.okData(Map.of("content", noteSettingService.getSettingContent()));
    }

    /**
     * 测试提醒功能
     */
    @RequestMapping(value = "/api/system/test/remind", method = RequestMethod.GET)
    public Result testRemind(@RequestParam String message) {
        if (ValueUtility.isBlank(message)) {
            throw new ControllerException("参数 message 不能为空");
        }
        this.remindGateway.remind(message);
        return Result.ok();
    }

    /**
     * 测试短信功能
     */
    @RequestMapping(value = "/api/system/test/sms", method = RequestMethod.GET)
    public Result testSms(@RequestParam String mobile, @RequestParam String code) {
        if (ValueUtility.isBlank(mobile)) {
            throw new ControllerException("参数 mobile 不能为空");
        }
        if (ValueUtility.isBlank(code)) {
            throw new ControllerException("参数 code 不能为空");
        }
        this.smsGateway.sendCode(mobile, code);
        return Result.ok();
    }
}
