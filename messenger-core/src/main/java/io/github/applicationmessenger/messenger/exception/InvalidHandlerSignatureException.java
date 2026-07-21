package io.github.applicationmessenger.messenger.exception;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public final class InvalidHandlerSignatureException extends RuntimeException {
    public InvalidHandlerSignatureException(String message) {
        super(message);
    }
}
