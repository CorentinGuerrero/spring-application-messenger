package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class MessageDispatchException extends RuntimeException {
    public MessageDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
