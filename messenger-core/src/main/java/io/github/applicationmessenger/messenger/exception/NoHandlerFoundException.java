package io.github.applicationmessenger.messenger.exception;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public final class NoHandlerFoundException extends RuntimeException {
    public NoHandlerFoundException(Class<?> messageType) {
        super("No handler found for message type " + messageType.getName());
    }
}
