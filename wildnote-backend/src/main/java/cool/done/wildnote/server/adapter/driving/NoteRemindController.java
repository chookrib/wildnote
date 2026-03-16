package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteRemindService;
import cool.done.wildnote.server.domain.NoteCron;
import cool.done.wildnote.server.utility.ValueUtility;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        List<String> jobIdList = cronList.stream()
                .filter(cron -> !ValueUtility.isBlank(cron.getJobId()))
                .map(NoteCron::getJobId)
                .toList();

        List<Map<String, Object>> activeCronList = new ArrayList<>();
        List<Map<String, Object>> inactiveCronList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (NoteCron item : cronList) {
            String jobId = item.getJobId();
            Long nextTime = jobMap.getOrDefault(jobId, null);
            if (nextTime != null && nextTime > currentTime) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("path", item.getPath());
                map.put("lineNumber", item.getLineNumber());
                map.put("cronExpression", item.getCronExpression());
                map.put("description", item.getDescription());
                map.put("jobId", item.getJobId());
                map.put("nextTime", ValueUtility.toLocalDateTime(nextTime));
                map.put("delayTime", ValueUtility.calcDelayTime(nextTime));
                activeCronList.add(map);
            } else {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("path", item.getPath());
                map.put("lineNumber", item.getLineNumber());
                map.put("cronExpression", item.getCronExpression());
                map.put("description", item.getDescription());
                map.put("jobId", item.getJobId());
                map.put("status", jobIdList.contains(item.getJobId())? "stopped" : "unscheduled");
                inactiveCronList.add(map);
            }
        }

        //List<Map<String, Object>> scheduledCronList = cronList.stream()
        //        .filter(cron -> !ValueUtility.isBlank(cron.getJobId()) && jobIdList.contains(cron.getJobId()))
        //        .map(cron -> {
        //            Map<String, Object> map = new java.util.HashMap<>();
        //            map.put("path", cron.getPath());
        //            map.put("lineNumber", cron.getLineNumber());
        //            map.put("cronExpression", cron.getCronExpression());
        //            map.put("description", cron.getDescription());
        //            map.put("jobId", cron.getJobId());
        //            map.put("nextTime",
        //                    ValueUtility.toLocalDateTime(jobMap.getOrDefault(cron.getJobId(), null))
        //            );
        //            map.put("delayTime",
        //                    ValueUtility.calcDelayTime(jobMap.getOrDefault(cron.getJobId(), null))
        //            );
        //            return map;
        //        }).toList();
        //List<Map<String, Object>> unscheduledCronList = cronList.stream()
        //        .filter(cron -> ValueUtility.isBlank(cron.getJobId()) || !jobIdList.contains(cron.getJobId()))
        //        .map(cron -> {
        //            Map<String, Object> map = new java.util.HashMap<>();
        //            map.put("path", cron.getPath());
        //            map.put("lineNumber", cron.getLineNumber());
        //            map.put("cronExpression", cron.getCronExpression());
        //            map.put("description", cron.getDescription());
        //            return map;
        //        }).toList();

        List<Map<String, Object>> dirtyJobList = jobMap.entrySet().stream()
                .filter(entry -> !jobIdList.contains(entry.getKey()))
                .map(entry -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("jobId", entry.getKey());
                    map.put("nextTime", ValueUtility.toLocalDateTime(entry.getValue()));
                    // map.put("delayTime", ValueUtility.calcDelayTime(entry.getValue()));
                    return map;
                }).toList();

        return Result.okData(Map.of(
                "activeCronList", activeCronList,
                "inactiveCronList", inactiveCronList,
                "dirtyJobList", dirtyJobList
                //, "jobMap", jobMap
        ));
    }
}
