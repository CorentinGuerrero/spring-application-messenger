package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public final class NoTransportFoundException extends RuntimeException {
    public NoTransportFoundException(String transportName) {
        super("No message transport found with name '" + transportName + "'");
    }
}
