package italiaken.fantasticbeasts.chizpurfle.configuration;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class ConfigurationException extends Exception {

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }
}
