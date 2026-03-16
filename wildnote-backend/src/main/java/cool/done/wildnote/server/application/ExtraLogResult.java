package cool.done.wildnote.server.application;

/**
 * 日志读取结果
 */
public class ExtraLogResult {
    public final String content;
    public final boolean hasMore;
    public final long offset;

    public ExtraLogResult(String content, boolean hasMore, long offset) {
        this.content = content;
        this.hasMore = hasMore;
        this.offset = offset;
    }
}
