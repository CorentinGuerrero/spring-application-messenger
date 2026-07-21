package io.github.applicationmessenger.messenger.routing;

import io.github.applicationmessenger.messenger.api.Spi;
import io.github.applicationmessenger.messenger.dispatch.BusType;

@Spi
public record MessageRoute(BusType busType, Class<?> messageType, String transportName) {
}
