package cool.done.wildnote.server.application;

import cool.done.wildnote.server.domain.*;
import cool.done.wildnote.server.utility.ValueUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 笔记提醒 Service
 */
@Component
public class NoteRemindService {

    private static final Logger logger = LoggerFactory.getLogger(NoteRemindService.class);

    // 存储笔记提醒计划任务的 List
    private List<NoteCron> cronList = java.util.Collections.synchronizedList(new ArrayList<>());

    private final RemindScheduler remindScheduler;
    private final ExtraLogService extraLogService;

    public NoteRemindService(
            //@Qualifier("remindTaskScheduler") RemindScheduler remindScheduler,
            @Qualifier("remindQuartzScheduler") RemindScheduler remindScheduler,
            ExtraLogService extraLogService
    ) {
        this.remindScheduler = remindScheduler;
        this.extraLogService = extraLogService;
    }

    /**
     * 取笔记提醒计划任务
     */
    public List<NoteCron> getCronList() {
        return cronList;
    }

    /**
     * 取笔记提醒计划任务已调度作业，返回值 key=计划任务作业Id，value=下次执行的时间戳
     */
    public Map<String, Long> getCronJobMap() {
        return remindScheduler.getAllJob();
    }

    /**
     * 调度笔记提醒作业
     */
    public void scheduleCron(String path, int lineNumber, String cronExpression, String description) {
        NoteCron cron = NoteCron.create(path, lineNumber, cronExpression, description);

        if (!ValueUtility.isBlank(cronExpression)) {
            try {
                String jobId = remindScheduler.addJob(cronExpression, description);
                cron.setJobId(jobId);
                extraLogService.logRemindInfo(String.format("调度笔记提醒作业成功: %s %s %s %s %s",
                        path, lineNumber, cronExpression, description, jobId
                ), logger);
            } catch (Exception ex) {
                extraLogService.logRemindError(
                        String.format("调度笔记提醒作业异常: %s %s %s %s %s",
                                path, lineNumber, cronExpression, description, ex.getMessage()
                        ), logger);
            }
        } else {
            extraLogService.logRemindError(String.format("调度笔记提醒作业失败: cron表达式空 %s %s %s",
                    path, lineNumber, description
            ), logger);
        }

        cronList.add(cron);
    }

    /**
     * 取消调度笔记提醒作业
     */
    public void unscheduleCron(String path) {
        List<NoteCron> list = cronList.stream()
                .filter(cron -> cron.getPath().equals(path))
                .toList();

        if (list.isEmpty()) {
            return;
        }

        for (NoteCron cron : list) {
            if (!ValueUtility.isBlank(cron.getJobId())) {
                try {
                    remindScheduler.deleteJob(cron.getJobId());
                    extraLogService.logRemindInfo(String.format("取消调度笔记提醒作业成功: %s %s %s %s",
                            cron.getPath(),
                            cron.getLineNumber(),
                            cron.getCronExpression(),
                            cron.getDescription()
                    ), logger);
                } catch (Exception ex) {
                    extraLogService.logRemindError(
                            String.format("取消调度笔记提醒作业异常: %s %s %s %s %s",
                                    cron.getPath(),
                                    cron.getLineNumber(),
                                    cron.getCronExpression(),
                                    cron.getDescription(),
                                    ex.getMessage()
                            ), logger);
                }
            }

            cronList.remove(cron);
        }
    }

    /**
     * 清除所有已调度笔记提醒作业
     */
    public void clearCron() {
        cronList.clear();
        try {
            remindScheduler.clearJob();
            extraLogService.logRemindInfo(String.format("清除所有已调度笔记提醒作业成功"), logger);
        } catch (Exception ex) {
            extraLogService.logRemindError(String.format("清除所有已调度笔记提醒作业异常: %s", ex.getMessage()), logger);
        }
    }
}

