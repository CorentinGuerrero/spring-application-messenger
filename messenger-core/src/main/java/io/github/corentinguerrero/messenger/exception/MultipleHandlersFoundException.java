package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class MultipleHandlersFoundException extends RuntimeException {
    public MultipleHandlersFoundException(Class<?> messageType) {
        super("Multiple handlers found for message type " + messageType.getName());
    }
}
