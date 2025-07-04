package cool.done.wildnote.server.domain;

/**
 * 短信接口
 */
public interface ISmsHandler {
    /**
     * 发送验证码短信
     */
    void sendCode(String mobile, String code);
}
