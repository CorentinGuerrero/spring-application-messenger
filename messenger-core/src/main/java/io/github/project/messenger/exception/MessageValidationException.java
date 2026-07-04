package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class MessageValidationException extends RuntimeException {
    public MessageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
