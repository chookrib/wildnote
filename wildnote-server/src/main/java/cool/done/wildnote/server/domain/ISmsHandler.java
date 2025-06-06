package cool.done.wildnote.server.domain;

/**
 * 短信接口
 */
public interface ISmsHandler {
    void send(String mobile, String message);
}
