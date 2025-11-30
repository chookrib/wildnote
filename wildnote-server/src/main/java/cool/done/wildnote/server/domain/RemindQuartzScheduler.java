package cool.done.wildnote.server.domain;

import cool.done.wildnote.server.application.NoteRemindService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 笔记提醒计划任务作业调度器
 * 使用 Quartz 实现
 */
@Component
public class RemindQuartzScheduler implements RemindScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RemindQuartzScheduler.class);

    private final RemindGateway remindGateway;
    private final Scheduler scheduler;
    private final String schedulerJobGroupName = "REMIND";

    public RemindQuartzScheduler(RemindGateway remindGateway) {
        this.remindGateway = remindGateway;
        try {
            this.scheduler = StdSchedulerFactory.getDefaultScheduler();
            this.scheduler.setJobFactory((bundle, scheduler) ->
                    (context) -> {
                        JobDataMap data = context.getJobDetail().getJobDataMap();
                        String message = data.getString("message");
                        this.remindGateway.remind(message);
                    });
            this.scheduler.start();
        } catch (SchedulerException ex) {
            throw new DomainException(String.format("RemindSchedulerQuartzImpl初始化异常: %s", ex.getMessage()), ex);
        }
    }

    @Override
    public String addJob(String cronExpression, String message) {
        String jobId = UUID.randomUUID().toString();
        JobDetail jobDetail = JobBuilder.newJob()
                .ofType(Job.class)
                .withIdentity(jobId, schedulerJobGroupName)
                .usingJobData("message", message)
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, schedulerJobGroupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            throw new DomainException(ex.getMessage(), ex);
        }
        return jobId;
    }

    @Override
    public void delJob(String jobId) {
        JobKey jobKey = new JobKey(jobId, schedulerJobGroupName);
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException ex) {
            throw new DomainException(ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, Long> getAllJob() {
        Map<String, Long> result = new HashMap<>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(schedulerJobGroupName));
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                Date nextTime = null;
                for (Trigger trigger : triggers) {
                    Date nextFireTime = trigger.getNextFireTime();
                    if (nextFireTime == null) {
                        continue;
                    }
                    if (nextTime == null) {
                        nextTime = nextFireTime;
                        continue;
                    }
                    if (nextFireTime.before(nextTime)) {
                        nextTime = nextFireTime;
                    }
                }

                if (nextTime != null)
                    result.put(jobKey.getName(), nextTime.getTime());
                else
                    result.put(jobKey.getName(), null);
            }
        } catch (SchedulerException ex) {
            throw new DomainException(ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public void clearJob() {
        // scheduler.clear();

        GroupMatcher<JobKey> matcher = GroupMatcher.jobGroupEquals(schedulerJobGroupName);
        try {
            Set<JobKey> jobKeySet = scheduler.getJobKeys(matcher);
            List<JobKey> jobKeyList = new ArrayList<>(jobKeySet);
            scheduler.deleteJobs(jobKeyList);
        } catch (SchedulerException ex) {
            throw new DomainException(ex.getMessage(), ex);
        }
    }
}


