package io.github.corentinguerrero.messenger.routing;

import io.github.corentinguerrero.messenger.api.Spi;
import io.github.corentinguerrero.messenger.dispatch.BusType;

@Spi
public record MessageRoute(BusType busType, Class<?> messageType, String transportName) {
}
