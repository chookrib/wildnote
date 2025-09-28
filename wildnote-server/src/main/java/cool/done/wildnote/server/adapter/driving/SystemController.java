package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.application.ExtraLogService;
import cool.done.wildnote.server.application.ExtraLogType;
import cool.done.wildnote.server.application.SettingService;
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
    private final SettingService settingService;
    private final ExtraLogService extraLogService;

    public SystemController(RemindGateway remindGateway, SmsGateway smsGateway,
                            SettingService settingService, ExtraLogService extraLogService) {
        this.remindGateway = remindGateway;
        this.smsGateway = smsGateway;
        this.settingService = settingService;
        this.extraLogService = extraLogService;
    }

    /**
     * 取配置
     */
    @RequestMapping(value = "/api/system/setting", method = RequestMethod.GET)
    public Result setting() {
        JsonNode settingJson = settingService.getSettingJson();
        return Result.okData(settingJson);
    }

    /**
     * 取最新提醒日志
     */
    @RequestMapping(value = "/api/system/log/type", method = RequestMethod.GET)
    public Result logType() {
        return Result.okData(
                Map.of("list", java.util.Arrays.asList(ExtraLogType.values()))
        );
    }

    /**
     * 取最新短信日志
     */
    @RequestMapping(value = "/api/system/log/get", method = RequestMethod.GET)
    public Result smsLogGet(@RequestParam String type,
                               @RequestParam(defaultValue = "0") long offset) {
        ExtraLogService.ReadLogResult logResult = extraLogService.readLog(ExtraLogType.parse(type), offset);
        return Result.okData(Map.of("result", logResult));
    }

    /**
     * 测试提醒功能
     */
    @RequestMapping(value = "/api/system/remind/test", method = RequestMethod.GET)
    public Result remindTest(@RequestParam String message) {
        if (ValueUtility.isBlank(message)) {
            throw new ControllerException("参数message不能为空");
        }
        this.remindGateway.remind(message);
        return Result.ok();
    }

    /**
     * 测试短信功能
     */
    @RequestMapping(value = "/api/system/sms/test", method = RequestMethod.GET)
    public Result smsTest(@RequestParam String mobile, @RequestParam String code) {
        if (ValueUtility.isBlank(mobile)) {
            throw new ControllerException("参数mobile不能为空");
        }
        if (ValueUtility.isBlank(code)) {
            throw new ControllerException("参数code不能为空");
        }
        this.smsGateway.sendCode(mobile, code);
        return Result.ok();
    }
}
