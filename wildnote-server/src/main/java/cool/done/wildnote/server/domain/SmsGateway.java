package cool.done.wildnote.server.domain;

/**
 * 短信 Gateway 接口
 */
public interface SmsGateway {
    /**
     * 发送验证码短信
     */
    void sendCode(String mobile, String code);
}
