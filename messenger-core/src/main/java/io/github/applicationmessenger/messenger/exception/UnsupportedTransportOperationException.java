package io.github.applicationmessenger.messenger.exception;

import io.github.applicationmessenger.messenger.api.PublicApi;
import io.github.applicationmessenger.messenger.dispatch.BusType;

@PublicApi
public final class UnsupportedTransportOperationException extends RuntimeException {
    public UnsupportedTransportOperationException(String transportName, BusType busType) {
        super("Transport '" + transportName + "' does not support " + busType + " messages");
    }
}
