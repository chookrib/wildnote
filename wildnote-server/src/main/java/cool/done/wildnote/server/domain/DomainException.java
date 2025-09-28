package cool.done.wildnote.server.domain;

/**
 * Domain Exception
 */
public class DomainException extends RuntimeException {

    public DomainException() { super(); }

    public DomainException(String message) {
        super(message);
    }
}