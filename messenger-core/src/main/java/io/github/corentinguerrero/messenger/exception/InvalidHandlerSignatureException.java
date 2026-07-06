package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class InvalidHandlerSignatureException extends RuntimeException {
    public InvalidHandlerSignatureException(String message) {
        super(message);
    }
}
