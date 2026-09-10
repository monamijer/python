// TmdbIndisponibleException.java
package main.java.com.monprojet.series.exception;

// Thrown when the TMDB API is unreachable or returns a 5xx — mapped to 503
public class TmdbIndisponibleException extends RuntimeException {
    public TmdbIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}