package cool.done.wildnote.server.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 笔记提醒计划任务
 * 使用 TaskScheduler 实现
 */
@Component
public class NoteRemindTaskScheduler implements NoteRemindScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NoteRemindTaskScheduler.class);

    private final RemindHandler remindHandler;
    private final TaskScheduler taskScheduler;
    private Map<String, ScheduledFuture<?>> scheduleMap = new ConcurrentHashMap<>();

    public NoteRemindTaskScheduler(RemindHandler remindHandler, TaskScheduler taskScheduler) {
        this.remindHandler = remindHandler;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 添加提醒计划任务
     */
    @Override
    public void add(String path, int lineNumber, String cron, String message) {
        String key = path + " | " + lineNumber + " | " + cron + " | " + message;

        CronTrigger cronTrigger;
        try {
            cronTrigger = new CronTrigger(cron);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {
                    remindHandler.remind(path + " | " + lineNumber + " | " + cron + " | " + message);
                },
                cronTrigger
        );
        scheduleMap.put(key, scheduledFuture);
    }

    /**
     * 清除所有提醒计划任务
     */
    @Override
    public void clearAll() {
        scheduleMap.values().forEach(scheduledFuture -> scheduledFuture.cancel(true));
        scheduleMap.clear();
    }

    /**
     * 删除提醒计划任务
     */
    @Override
    public void remove(String path) {
        scheduleMap.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(path + " | ")) {
                entry.getValue().cancel(true);
                return true;
            }
            return false;
        });
    }

    /**
     * 取所有提醒计划任务
     */
    @Override
    public List<NoteRemindCron> getAll() {
        //return scheduleMap.keySet().stream().toList();
        return scheduleMap.keySet().stream()
                .map(key -> {
                    String[] parts = key.split("\\|");
                    String path = parts.length >= 0 ? parts[0].trim() : "";
                    String lineNumber = parts.length >= 1 ? parts[1].trim() : "";
                    String cron = parts.length >= 2 ? parts[2].trim() : "";
                    String message = parts.length >= 3 ? parts[3].trim() : "";
                    long delayTime = scheduleMap.get(key).getDelay(TimeUnit.MILLISECONDS);
                    Long nextTime = null;
                    if (delayTime >= 0) {
                        nextTime = System.currentTimeMillis() + delayTime;
                    }
                    return new NoteRemindCron(path, lineNumber, cron, message, nextTime, delayTime);
                })
                //.filter(cron -> cron != null)
                .toList();
    }
}
