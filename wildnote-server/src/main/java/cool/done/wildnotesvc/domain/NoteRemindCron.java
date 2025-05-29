package cool.done.wildnote.server.domain;

/**
 * 笔记提醒计划任务
 */
public class NoteRemindCron {
    public String path;
    public String lineNumber;
    public String cron;
    public String message;
    public Long nextTime;
    public long delayTime;

    public NoteRemindCron(String path, String lineNumber, String cron, String message,
                          Long nextTime, long delayTime) {
        this.path = path;
        this.lineNumber = lineNumber;
        this.cron = cron;
        this.message = message;
        this.nextTime = nextTime;
        this.delayTime = delayTime;
    }
}
