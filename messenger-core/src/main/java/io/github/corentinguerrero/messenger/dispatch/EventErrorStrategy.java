package io.github.corentinguerrero.messenger.dispatch;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public enum EventErrorStrategy {
    FAIL_FAST,
    CONTINUE,
    IGNORE
}
