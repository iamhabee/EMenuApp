package com.arke.sdk.util.printer;

/**
 * Page not load exception.
 *
 * @author feiq
 */

public class PageNotLoadedException extends RuntimeException {
    public PageNotLoadedException() {
    }

    public PageNotLoadedException(String message) {
        super(message);
    }

    public PageNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PageNotLoadedException(Throwable cause) {
        super(cause);
    }
}
