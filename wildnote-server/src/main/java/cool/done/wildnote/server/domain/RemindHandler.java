package cool.done.wildnote.server.domain;

/**
 * 提醒接口
 */
public interface RemindHandler {
    /**
     * 发送提醒消息
     */
    void remind(String message);
}
