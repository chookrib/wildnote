package cool.done.wildnote.server.adapter.driving;

/**
 * Controller Exception
 */
public class ControllerException extends RuntimeException {

    public ControllerException() { super(); }

    public ControllerException(String message) {
        super(message);
    }
}