package io.github.semanticpie.derezhor.externalAgents.loafLoader.services.sync;

public class SyncException extends RuntimeException {
    public SyncException() {
    }

    public SyncException(String message) {
        super(message);
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyncException(Throwable cause) {
        super(cause);
    }

    public SyncException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
