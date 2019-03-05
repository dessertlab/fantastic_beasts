package italiaken.fantasticbeasts.chizpurfle.infiltration;

/**
 * Created by ken on 24/11/17 for fantastic_beasts
 */

public class MethodCallerException extends InfiltrationException {
    public MethodCallerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodCallerException(Throwable cause) {
        super(cause);
    }

    public MethodCallerException(String message) {
        super(message);
    }
}
