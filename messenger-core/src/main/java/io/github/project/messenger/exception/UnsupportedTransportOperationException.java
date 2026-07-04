package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;
import io.github.project.messenger.dispatch.BusType;

@PublicApi
public final class UnsupportedTransportOperationException extends RuntimeException {
    public UnsupportedTransportOperationException(String transportName, BusType busType) {
        super("Transport '" + transportName + "' does not support " + busType + " messages");
    }
}
