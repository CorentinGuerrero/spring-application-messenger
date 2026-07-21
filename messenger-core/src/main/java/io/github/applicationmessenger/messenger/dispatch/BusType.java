package io.github.applicationmessenger.messenger.dispatch;

import io.github.applicationmessenger.messenger.api.Spi;

@Spi
public enum BusType {
    COMMAND,
    QUERY,
    EVENT
}
