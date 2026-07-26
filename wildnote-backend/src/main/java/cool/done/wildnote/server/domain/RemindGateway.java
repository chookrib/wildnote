package cool.done.wildnote.server.domain;

/**
 * 提醒 Gateway 接口
 */
public interface RemindGateway {

    /**
     * 提醒通知
     */
    void notify(String message);
}
