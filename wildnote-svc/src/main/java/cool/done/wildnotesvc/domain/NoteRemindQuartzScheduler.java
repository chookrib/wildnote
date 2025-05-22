package cool.done.wildnotesvc.domain;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
                        String message = data.getString("remindMessage");
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
                    .usingJobData("message", message)
                    .usingJobData("remindMessage", path + " | " + lineNumber + " | " + cron + " | " + message)
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
        //    scheduler.standby();
        //    for (String groupName : scheduler.getJobGroupNames()) {
        //        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
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
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);

                    //List<String> crons = new ArrayList<>();
                    //List<String> nextTimes = new ArrayList<>();
                    //List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    //for (Trigger trigger : triggers) {
                    //    if (trigger instanceof CronTrigger cronTrigger) {
                    //        crons.add(cronTrigger.getCronExpression());
                    //        nextTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trigger.getNextFireTime()));
                    //    }
                    //}
                    //NoteRemindCron remindCron = new NoteRemindCron(groupName, jobKey.getName(),
                    //        String.join(" , ", crons),
                    //        jobDetail.getJobDataMap().getString("message"),
                    //        String.join(" , ", nextTimes));

                    String cron = "";
                    Long nextTime = null;
                    long delayTime = 0;
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        if (trigger instanceof CronTrigger cronTrigger) {
                            cron = cronTrigger.getCronExpression();
                            nextTime = trigger.getNextFireTime().getTime();
                            delayTime = nextTime - System.currentTimeMillis();
                            break;
                        }
                    }
                    NoteRemindCron remindCron = new NoteRemindCron(groupName, jobKey.getName(),
                            cron,
                            jobDetail.getJobDataMap().getString("message"),
                            nextTime, delayTime);

                    result.add(remindCron);
                }
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}


