package cool.done.wildnote.domain;

/**
 * 未登录异常
 */
public class NotLoginException extends RuntimeException {
    public NotLoginException(String message) {
        super(message);
    }
}
