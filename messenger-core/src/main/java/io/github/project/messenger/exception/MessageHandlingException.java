package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class MessageHandlingException extends RuntimeException {
    public MessageHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
