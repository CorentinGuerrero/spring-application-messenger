package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class NoHandlerFoundException extends RuntimeException {
    public NoHandlerFoundException(Class<?> messageType) {
        super("No handler found for message type " + messageType.getName());
    }
}
