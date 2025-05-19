package cool.done.wildnotesvc.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 笔记提醒计划任务
 * 使用TaskScheduler实现
 */
@Component
public class NoteRemindTaskScheduler implements INoteRemindScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NoteRemindTaskScheduler.class);

    private final IReminder reminder;
    private final TaskScheduler taskScheduler;
    private Map<String, ScheduledFuture<?>> schdMap = new ConcurrentHashMap<>();

    public NoteRemindTaskScheduler(IReminder reminder, TaskScheduler taskScheduler) {
        this.reminder = reminder;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 添加提醒计划任务
     */
    public void add(String path, int lineNumber, String cron, String message) {
        String key = path + " | " + lineNumber + " | " + cron;

        CronTrigger cronTrigger;
        try {
            cronTrigger = new CronTrigger(cron);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {
                    reminder.remind(path + " | " + lineNumber + " | " + cron + "|" + message);
                },
                cronTrigger
        );
        schdMap.put(key, scheduledFuture);
    }

    /**
     * 清除所有提醒计划任务
     */
    public void clearAll() {
        schdMap.values().forEach(scheduledFuture -> scheduledFuture.cancel(true));
        schdMap.clear();
    }

    /**
     * 删除提醒计划任务
     */
    public void remove(String path) {
        schdMap.entrySet().removeIf(entry -> {
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
    public List<String> getAll() {
        return schdMap.keySet().stream().toList();
    }
}
