package cool.done.wildnote.server.domain;

import java.util.UUID;

/**
 * 笔记计划任务
 */
public class NoteCron {

    private String path;
    private int lineNumber;
    private String cronExpression;
    private String description;
    private String jobId;

    public String getPath() { return this.path; }

    public int getLineNumber() { return this.lineNumber; }

    public String getCronExpression() { return this.cronExpression; }

    public String getDescription() { return this.description; }

    public String getJobId() { return this.jobId; }

    private NoteCron() { }

    /**
     * 创建
     */
    public static NoteCron create(String path, int lineNumber, String cronExpression, String description) {
        NoteCron cron = new NoteCron();
        cron.path = path;
        cron.lineNumber = lineNumber;
        cron.cronExpression = cronExpression;
        cron.description = description;
        cron.jobId = "";
        return cron;
    }

    /**
     * 设置 JobId
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
