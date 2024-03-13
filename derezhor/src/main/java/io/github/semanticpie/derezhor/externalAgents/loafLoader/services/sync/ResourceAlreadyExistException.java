package io.github.semanticpie.derezhor.externalAgents.loafLoader.services.sync;

public class ResourceAlreadyExistException extends RuntimeException{
    public ResourceAlreadyExistException() {
    }

    public ResourceAlreadyExistException(String message) {
        super(message);
    }

    public ResourceAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceAlreadyExistException(Throwable cause) {
        super(cause);
    }
}
