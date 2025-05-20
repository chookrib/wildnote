package cool.done.wildnotesvc.domain;

/**
 * 笔记提醒计划任务
 */
public class NoteRemindCron {
    public String path;
    public String lineNumber;
    public String cron;
    public String message;
    public String nextTime;

    public NoteRemindCron(String path, String lineNumber, String cron, String message, String nextTime) {
        this.path = path;
        this.lineNumber = lineNumber;
        this.cron = cron;
        this.message = message;
        this.nextTime = nextTime;
    }
}
