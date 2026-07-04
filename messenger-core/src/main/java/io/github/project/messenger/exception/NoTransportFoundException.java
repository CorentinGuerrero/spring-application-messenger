package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class NoTransportFoundException extends RuntimeException {
    public NoTransportFoundException(String transportName) {
        super("No message transport found with name '" + transportName + "'");
    }
}
