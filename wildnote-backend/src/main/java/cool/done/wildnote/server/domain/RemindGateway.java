package cool.done.wildnote.server.domain;

/**
 * 提醒 Gateway 接口
 */
public interface RemindGateway {

    /**
     * 发送提醒消息
     */
    void remind(String message);
}
