package cool.done.wildnote.server.application;

import java.util.UUID;

/**
 * 笔记计划任务
 */
public class NoteCronDto {

    private String id;
    private String path;
    private int lineNumber;
    private String cronExpression;
    private String description;
    private boolean isActive;
    private Long nextTimestamp;
    private Long delayTimestamp;

    public String getId() { return this.id; }

    public String getPath() { return this.path; }

    public int getLineNumber() { return this.lineNumber; }

    public String getCronExpression() { return this.cronExpression; }

    public String getDescription() { return this.description; }

    public boolean isActive() { return isActive; }

    public Long getNextTimestamp() { return this.nextTimestamp; }

    public Long getDelayTimestamp() { return this.delayTimestamp; }

    private NoteCronDto() { }

    /**
     * 创建
     */
    public static NoteCronDto create(String path, int lineNumber, String cronExpression, String description) {
        NoteCronDto cron = new NoteCronDto();
        cron.id = UUID.randomUUID().toString();
        cron.path = path;
        cron.lineNumber = lineNumber;
        cron.cronExpression = cronExpression;
        cron.description = description;
        cron.isActive = false;
        cron.nextTimestamp = null;
        cron.delayTimestamp = null;
        return cron;
    }
}
