package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class MessageValidationException extends RuntimeException {
    public MessageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
