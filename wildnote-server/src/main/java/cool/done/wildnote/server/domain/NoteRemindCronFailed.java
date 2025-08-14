package cool.done.wildnote.server.domain;

/**
 * 笔记提醒计划任务，添加失败的
 */
public class NoteRemindCronFailed {
    private String path;
    private String lineNumber;
    private String cron;
    private String message;

    public String getPath() { return this.path; }

    public String getLineNumber() { return this.lineNumber; }

    public String getCron() { return this.cron; }

    public String getMessage() { return this.message; }

    public NoteRemindCronFailed(String path, String lineNumber, String cron, String message) {
        this.path = path;
        this.lineNumber = lineNumber;
        this.cron = cron;
        this.message = message;
    }
}
