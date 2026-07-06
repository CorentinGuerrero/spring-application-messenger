package io.github.corentinguerrero.messenger.routing;

import io.github.corentinguerrero.messenger.api.Spi;
import io.github.corentinguerrero.messenger.dispatch.BusType;

@Spi
public interface MessageRouter {
    MessageRoute routeFor(Object message, BusType busType);
}
