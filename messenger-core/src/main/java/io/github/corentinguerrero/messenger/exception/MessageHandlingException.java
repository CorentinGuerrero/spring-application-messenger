package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class MessageHandlingException extends RuntimeException {
    public MessageHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
