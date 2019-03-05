package italiaken.fantasticbeasts.chizpurfle.fuzz.generators;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class ValueGeneratorException extends Exception {

    public ValueGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueGeneratorException(Throwable cause) {
        super(cause);
    }

    public ValueGeneratorException(String message) {
        super(message);
    }
}
