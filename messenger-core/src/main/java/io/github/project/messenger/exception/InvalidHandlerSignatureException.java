package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class InvalidHandlerSignatureException extends RuntimeException {
    public InvalidHandlerSignatureException(String message) {
        super(message);
    }
}
