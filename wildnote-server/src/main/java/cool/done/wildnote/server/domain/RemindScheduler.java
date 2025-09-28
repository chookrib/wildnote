package cool.done.wildnote.server.domain;

import java.util.Map;

/**
 * 用于提醒的计划任务作业调度器接口
 */
public interface RemindScheduler {

    /**
     * 添加提醒计划任务调度
     */
    String addJob(String cronExpression, String message);

    /**
     * 根据作业Id删除提醒计划任务调度
     */
    void dropJob(String jobKey);

    /**
     * 取所有已调度的提醒计划任务，返回值 key=计划任务作业Id，value=下次执行的时间戳
     */
    Map<String, Long> getAllJob();

    /**
     * 清除所有调度的提醒计划任务
     */
    void clearJob();
}
