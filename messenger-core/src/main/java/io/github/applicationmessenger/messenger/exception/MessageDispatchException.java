package io.github.applicationmessenger.messenger.exception;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public final class MessageDispatchException extends RuntimeException {
    public MessageDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
