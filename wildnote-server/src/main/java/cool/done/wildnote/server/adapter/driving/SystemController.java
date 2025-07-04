package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.domain.IRemindHandler;
import cool.done.wildnote.server.domain.ISmsHandler;
import cool.done.wildnote.server.domain.NoteService;
import cool.done.wildnote.server.domain.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

@RestController
public class SystemController {

    @Value("${wildnote.remind-log:}")
    private String remindLog;

    @Value("${wildnote.sms-log:}")
    private String smsLog;

    private final IRemindHandler remindHandler;

    private final ISmsHandler smsHandler;


    public SystemController(IRemindHandler remindHandler, ISmsHandler smsHandler) {
        this.remindHandler = remindHandler;
        this.smsHandler = smsHandler;
    }

    /**
     * 取最新提醒日志
     */
    @RequestMapping(value = "/api/system/remind/recent-log", method = RequestMethod.GET)
    public Result systemRemindRecentLog(@RequestParam(defaultValue = "1024") int size) {
        try (RandomAccessFile raf = new RandomAccessFile(remindLog, "r")) {
            long fileLength = raf.length();
            long start = Math.max(0, fileLength - size);
            raf.seek(start);
            byte[] buffer = new byte[(int)Math.min(size, fileLength)];
            raf.readFully(buffer);

            String result = new String(buffer, StandardCharsets.UTF_8);
            if(start > 0) {
                //去除被载断的行
                result = result.substring(result.indexOf("\n") + 1);
            }
            //反转行顺序
            result = result.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                java.util.Collections.reverse(list);
                                return String.join("\n", list);
                            }
                    ));
            return Result.successData(result);
        } catch (Exception e) {
            return Result.successData(e.getMessage());
        }
    }

    /**
     * 测试提醒功能
     */
    @RequestMapping(value = "/api/system/remind/test", method = RequestMethod.GET)
    public Result systemRemindTest(@RequestParam String message) {
        if(StringUtils.isEmpty(message)) {
            throw new ValidationException("提醒消息不能为空");
        }
        this.remindHandler.remind(message);
        return Result.success();
    }

    /**
     * 取最新短信日志
     */
    @RequestMapping(value = "/api/system/sms/recent-log", method = RequestMethod.GET)
    public Result systemSmsRecentLog(@RequestParam(defaultValue = "1024") int size) {
        try (RandomAccessFile raf = new RandomAccessFile(smsLog, "r")) {
            long fileLength = raf.length();
            long start = Math.max(0, fileLength - size);
            raf.seek(start);
            byte[] buffer = new byte[(int)Math.min(size, fileLength)];
            raf.readFully(buffer);

            String result = new String(buffer, StandardCharsets.UTF_8);
            if(start > 0) {
                //去除被载断的行
                result = result.substring(result.indexOf("\n") + 1);
            }
            //反转行顺序
            result = result.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                java.util.Collections.reverse(list);
                                return String.join("\n", list);
                            }
                    ));
            return Result.successData(result);
        } catch (Exception e) {
            return Result.successData(e.getMessage());
        }
    }

    /**
     * 测试短信功能
     */
    @RequestMapping(value = "/api/system/sms/test", method = RequestMethod.GET)
    public Result systemSmsTest(@RequestParam String mobile, @RequestParam String code) {
        if(StringUtils.isEmpty(mobile)) {
            throw new ValidationException("mobile不能为空");
        }
        if(StringUtils.isEmpty(code)) {
            throw new ValidationException("code不能为空");
        }
        this.smsHandler.sendCode(mobile, code);
        return Result.success();
    }
}
