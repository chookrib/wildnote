package cool.done.wildnotesvc.domain;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 笔记提醒计划任务
 * 使用 Quartz 实现
 */
@Component
public class NoteRemindQuartzScheduler implements INoteRemindScheduler {
    private static final Logger logger = LoggerFactory.getLogger(NoteRemindQuartzScheduler.class);

    private final IReminder reminder;
    private final Scheduler scheduler;

    public NoteRemindQuartzScheduler(IReminder reminder) {
        this.reminder = reminder;
        try {
            this.scheduler = StdSchedulerFactory.getDefaultScheduler();
            this.scheduler.setJobFactory((bundle, scheduler) ->
                    (context) -> {
                        JobDataMap data = context.getJobDetail().getJobDataMap();
                        String message = data.getString("message");
                        this.reminder.remind(message);
                    });
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("NoteRemindQuartzScheduler初始化失败", e);
        }
    }

    @Override
    public void add(String path, int lineNumber, String cron, String message) {
        try {
            JobDetail jobDetail = JobBuilder.newJob()
                    .ofType(Job.class)
                    .withIdentity(String.valueOf(lineNumber), path)
                    .usingJobData("message", path + " | " + lineNumber + " | " + cron + " | " + message)
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(String.valueOf(lineNumber), path)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearAll() {
        try {
            scheduler.clear();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        //try {
        //    // 暂停调度器
        //    scheduler.standby();
        //    // 获取所有Job组
        //    for (String groupName : scheduler.getJobGroupNames()) {
        //        // 获取组内所有JobKey
        //        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
        //            // 删除Job及其关联的Trigger
        //            scheduler.deleteJob(jobKey);
        //        }
        //    }
        //} catch (SchedulerException e) {
        //    throw new RuntimeException(e);
        //}
    }

    @Override
    public void remove(String path) {
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(path);
            Set<JobKey> jobKeySet = scheduler.getJobKeys(matcher);
            List<JobKey> jobKeyList = new ArrayList<>(jobKeySet);
            scheduler.deleteJobs(jobKeyList);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        //try {
        //    for (String groupName : scheduler.getJobGroupNames()) {
        //        if (!groupName.equals(path)) {
        //            continue;
        //        }
        //        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
        //            scheduler.deleteJob(jobKey);
        //        }
        //    }
        //} catch (SchedulerException e) {
        //    throw new RuntimeException(e);
        //}
    }

    @Override
    public List<NoteRemindCron> getAll() {
        List<NoteRemindCron> result = new ArrayList<>();

        try {
            // 获取所有Job组
            for (String groupName : scheduler.getJobGroupNames()) {
                // 获取组内所有JobKey
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                    List<String> cronExpressions = new ArrayList<>();
                    List<String> nextTimes = new ArrayList<>();
                    // 获取Job对应的Trigger
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        // 获取Trigger的CronSchedule表达式
                        if (trigger instanceof CronTrigger cronTrigger) {
                            cronExpressions.add(cronTrigger.getCronExpression());
                            nextTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trigger.getNextFireTime()));
                        }
                    }

                    NoteRemindCron cron = new NoteRemindCron(groupName, jobKey.getName(),
                            String.join(" , ", cronExpressions),
                            jobDetail.getJobDataMap().getString("message"),
                            String.join(" , ", nextTimes));

                    result.add(cron);
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}


