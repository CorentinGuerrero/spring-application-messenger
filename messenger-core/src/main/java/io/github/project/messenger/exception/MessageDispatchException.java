package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class MessageDispatchException extends RuntimeException {
    public MessageDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
