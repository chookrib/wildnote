package cool.done.wildnote.server.application;

/**
 * 日志读取结果
 */
public class ExtraLogResult {
    public final String log;
    public final boolean hasMore;
    public final long moreOffsetFromEnd;

    public ExtraLogResult(String log, boolean hasMore, long moreOffsetFromEnd) {
        this.log = log;
        this.hasMore = hasMore;
        this.moreOffsetFromEnd = moreOffsetFromEnd;
    }
}
