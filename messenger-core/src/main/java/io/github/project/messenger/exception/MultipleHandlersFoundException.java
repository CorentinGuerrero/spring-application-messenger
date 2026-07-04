package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class MultipleHandlersFoundException extends RuntimeException {
    public MultipleHandlersFoundException(Class<?> messageType) {
        super("Multiple handlers found for message type " + messageType.getName());
    }
}
