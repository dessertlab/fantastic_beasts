package italiaken.fantasticbeasts.chizpurfle.infiltration;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class InfiltrationException extends Exception {

    public InfiltrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfiltrationException(Throwable cause) {
        super(cause);
    }

    public InfiltrationException(String message) {
        super(message);
    }
}
