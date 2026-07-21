package io.github.applicationmessenger.messenger.dispatch;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public enum EventErrorStrategy {
    FAIL_FAST,
    CONTINUE,
    IGNORE
}
