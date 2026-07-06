package io.github.corentinguerrero.messenger.dispatch;

import io.github.corentinguerrero.messenger.api.Spi;

@Spi
public enum BusType {
    COMMAND,
    QUERY,
    EVENT
}
