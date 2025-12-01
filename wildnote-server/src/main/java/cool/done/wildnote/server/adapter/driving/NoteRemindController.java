package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteRemindService;
import cool.done.wildnote.server.domain.NoteCron;
import cool.done.wildnote.server.utility.ValueUtility;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * 笔记提醒 Controller
 */
@RestController
public class NoteRemindController {

    private final NoteRemindService noteRemindService;

    public NoteRemindController(NoteRemindService noteRemindService) {
        this.noteRemindService = noteRemindService;
    }

    /**
     * 取所有笔记提醒计划任务
     */
    @RequestMapping(value = "/api/remind/all", method = RequestMethod.GET)
    public Result remindAll() {
        List<NoteCron> cronList = noteRemindService.getCronList();
        Map<String, Long> jobMap = noteRemindService.getCronJobMap();
        List<Map<String, Object>> scheduledCronList = cronList.stream()
                .filter(cron -> !ValueUtility.isBlank(cron.getJobId()))
                .map(cron -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("path", cron.getPath());
                    map.put("lineNumber", cron.getLineNumber());
                    map.put("cronExpression", cron.getCronExpression());
                    map.put("description", cron.getDescription());
                    map.put("jobId", cron.getJobId());
                    map.put("nextTime",
                            ValueUtility.toLocalDateTime(jobMap.getOrDefault(cron.getJobId(), null))
                    );
                    map.put("delayTime",
                            ValueUtility.calcDelayTime(jobMap.getOrDefault(cron.getJobId(), null))
                    );
                    return map;
                }).toList();
        List<Map<String, Object>> unscheduledCronList = cronList.stream()
                .filter(cron -> ValueUtility.isBlank(cron.getJobId()))
                .map(cron -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("path", cron.getPath());
                    map.put("lineNumber", cron.getLineNumber());
                    map.put("cronExpression", cron.getCronExpression());
                    map.put("description", cron.getDescription());
                    return map;
                }).toList();
        List<String> jobIdList = cronList.stream()
                .filter(cron -> !ValueUtility.isBlank(cron.getJobId()))
                .map(NoteCron::getJobId)
                .toList();
        List<Map<String, Object>> isolatedJobList = jobMap.entrySet().stream()
                .filter(entry -> !jobIdList.contains(entry.getKey()))
                .map(entry -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("jobId", entry.getKey());
                    map.put("nextTime", ValueUtility.toLocalDateTime(entry.getValue()));
                    // map.put("delayTime", ValueUtility.calcDelayTime(entry.getValue()));
                    return map;
                }).toList();
        return Result.okData(Map.of(
                "scheduledCronList", scheduledCronList,
                "unscheduledCronList", unscheduledCronList,
                //"jobMap", jobMap,
                "isolatedJobList", isolatedJobList
        ));
    }
}
