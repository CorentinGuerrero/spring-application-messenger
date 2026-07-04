package io.github.project.messenger.routing;

import io.github.project.messenger.api.Spi;
import io.github.project.messenger.dispatch.BusType;

@Spi
public record MessageRoute(BusType busType, Class<?> messageType, String transportName) {
}
