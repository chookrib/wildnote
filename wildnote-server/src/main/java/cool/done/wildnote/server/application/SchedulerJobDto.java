package cool.done.wildnote.server.application;

/**
 * 计划任务已调度作业
 */
public class SchedulerJobDto {
    private String id;
    private Long nextTimestamp;
    private Long delayTimestamp;

    public String getId() { return this.id; }

    public Long getNextTimestamp() { return this.nextTimestamp; }

    public Long getDelayTimestamp() { return this.delayTimestamp; }

    private SchedulerJobDto() { }

    /**
     * 创建
     */
    public static SchedulerJobDto create(String id, Long nextTimestamp, Long delayTimestamp) {
        SchedulerJobDto job = new SchedulerJobDto();
        job.id = id;
        job.nextTimestamp = nextTimestamp;
        job.delayTimestamp = delayTimestamp;
        return job;
    }
}
