package cool.done.wildnote.server.application;

/**
 * Application Exception
 */
public class ApplicationException extends RuntimeException {

    public ApplicationException() { super(); }

    public ApplicationException(String message) {
        super(message);
    }
}