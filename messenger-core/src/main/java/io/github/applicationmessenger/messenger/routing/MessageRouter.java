package io.github.applicationmessenger.messenger.routing;

import io.github.applicationmessenger.messenger.api.Spi;
import io.github.applicationmessenger.messenger.dispatch.BusType;

@Spi
public interface MessageRouter {
    MessageRoute routeFor(Object message, BusType busType);
}
