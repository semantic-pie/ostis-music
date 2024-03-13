package io.github.semanticpie.orchestrator.orchestrator.exceptions;

public class OrchestratorException extends RuntimeException {
    public OrchestratorException() {
    }

    public OrchestratorException(String message) {
        super(message);
    }

    public OrchestratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrchestratorException(Throwable cause) {
        super(cause);
    }
}
