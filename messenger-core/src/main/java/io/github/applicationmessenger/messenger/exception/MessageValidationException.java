package io.github.applicationmessenger.messenger.exception;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public final class MessageValidationException extends RuntimeException {
    public MessageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
