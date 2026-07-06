package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class NoHandlerFoundException extends RuntimeException {
    public NoHandlerFoundException(Class<?> messageType) {
        super("No handler found for message type " + messageType.getName());
    }
}
