package cool.done.wildnote.server.domain;

import java.util.List;

/**
 * 记提醒计划任务接口
 */
public interface INoteRemindScheduler {
    /**
     * 添加提醒计划任务
     */
    void add(String path, int lineNumber, String cron, String message);

    /**
     * 清除所有提醒计划任务
     */
    void clearAll();

    /**
     * 删除指定笔记的提醒Cron
     */
    void remove(String path);

    /**
     * 取所有提醒计划任务
     */
    List<NoteRemindCron> getAll();
}
