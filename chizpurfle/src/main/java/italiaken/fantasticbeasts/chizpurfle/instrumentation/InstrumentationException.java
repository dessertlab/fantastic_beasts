package italiaken.fantasticbeasts.chizpurfle.instrumentation;

import java.io.IOException;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class InstrumentationException extends Exception {

    public InstrumentationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstrumentationException(Throwable cause) {
        super(cause);
    }

    public InstrumentationException(String message) {
        super(message);
    }
}
