package cool.done.wildnote.server.adapter.driving;

/**
 * 未登录 Exception
 */
public class NotLoginException extends RuntimeException {

    public NotLoginException() { super(); }

    public NotLoginException(String message) {
        super(message);
    }
}
