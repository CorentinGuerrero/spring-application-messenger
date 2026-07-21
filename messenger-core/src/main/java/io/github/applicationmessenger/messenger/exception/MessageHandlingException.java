package io.github.applicationmessenger.messenger.exception;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public final class MessageHandlingException extends RuntimeException {
    public MessageHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
