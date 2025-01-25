package alix.loaders.classloader;

public final class AlixLoadingException extends RuntimeException {

    public AlixLoadingException(String message) {
        super(message);
    }

    public AlixLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}