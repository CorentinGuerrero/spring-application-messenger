package io.github.project.messenger.routing;

import io.github.project.messenger.api.Spi;
import io.github.project.messenger.dispatch.BusType;

@Spi
public interface MessageRouter {
    MessageRoute routeFor(Object message, BusType busType);
}
