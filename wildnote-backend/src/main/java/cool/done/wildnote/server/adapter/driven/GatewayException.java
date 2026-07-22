package cool.done.wildnote.server.adapter.driven;

/**
 * Gateway Exception
 */
public class GatewayException extends RuntimeException {

    public GatewayException() { super(); }

    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) { super(message, cause); }

    public GatewayException(Throwable cause) { super(cause); }
}