package io.github.project.messenger.dispatch;

import io.github.project.messenger.api.Spi;

@Spi
public enum BusType {
    COMMAND,
    QUERY,
    EVENT
}
