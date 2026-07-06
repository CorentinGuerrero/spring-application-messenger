package io.github.corentinguerrero.messenger.exception;

import io.github.corentinguerrero.messenger.api.PublicApi;
import io.github.corentinguerrero.messenger.dispatch.BusType;

@PublicApi
public final class UnsupportedTransportOperationException extends RuntimeException {
    public UnsupportedTransportOperationException(String transportName, BusType busType) {
        super("Transport '" + transportName + "' does not support " + busType + " messages");
    }
}
