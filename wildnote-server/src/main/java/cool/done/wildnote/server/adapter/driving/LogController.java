package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.ExtraLogResult;
import cool.done.wildnote.server.application.ExtraLogService;
import cool.done.wildnote.server.application.ExtraLogType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 日志 Controller
 */
@RestController
public class LogController {

    private final ExtraLogService extraLogService;

    public LogController(ExtraLogService extraLogService) {
        this.extraLogService = extraLogService;
    }

    /**
     * 取日志类型
     */
    @RequestMapping(value = "/api/log/type", method = RequestMethod.GET)
    public Result logType() {
        return Result.okData(
                Map.of("list", java.util.Arrays.asList(ExtraLogType.values()))
        );
    }

    /**
     * 取日志
     */
    @RequestMapping(value = "/api/log/get", method = RequestMethod.GET)
    public Result logGet(HttpServletRequest request) {
        String type = RequestValueHelper.getRequestParamStringTrimReq(request,"type");
        long offset = RequestValueHelper.getRequestParamLongOrDefault(request, 0, "offset");

        ExtraLogResult logResult = extraLogService.readLog(ExtraLogType.getByName(type), offset);
        return Result.okData(Map.of("result", logResult));
    }
}
