package cool.done.wildnote.server.domain;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 笔记提醒计划任务作业调度器
 * 使用 Spring TaskScheduler 实现
 */
@Component
public class RemindTaskScheduler implements RemindScheduler {

    private final RemindGateway remindGateway;
    private final TaskScheduler taskScheduler;

    // TaskScheduler 不支持获取作业，使用 Map 保存作业
    private Map<String, ScheduledFuture<?>> jobMap = new ConcurrentHashMap<>();

    public RemindTaskScheduler(RemindGateway remindGateway, TaskScheduler taskScheduler) {
        this.remindGateway = remindGateway;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public String addJob(String cronExpression, String message) {
        String jobId = UUID.randomUUID().toString();
        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {
                    remindGateway.remind(message);
                },
                cronTrigger
        );
        jobMap.put(jobId, scheduledFuture);
        return jobId;
    }

    @Override
    public void dropJob(String jobId) {
        jobMap.entrySet().removeIf(entry -> {
            if (entry.getKey().equals(jobId)) {
                entry.getValue().cancel(true);
                return true;
            }
            return false;
        });
    }

    @Override
    public Map<String, Long> getAllJob() {
        Map<String, Long> result = new HashMap<>();
        jobMap.forEach((key, scheduledFuture) -> {
            long delayMilliSeconds = scheduledFuture.getDelay(TimeUnit.MILLISECONDS);
            result.put(key, System.currentTimeMillis() + delayMilliSeconds);
        });
        return result;
    }

    @Override
    public void clearJob() {
        jobMap.values().forEach(
                scheduledFuture -> scheduledFuture.cancel(true)
        );
        jobMap.clear();
    }
}
