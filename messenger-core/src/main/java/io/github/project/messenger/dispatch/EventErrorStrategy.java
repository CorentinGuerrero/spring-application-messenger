package io.github.project.messenger.dispatch;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public enum EventErrorStrategy {
    FAIL_FAST,
    CONTINUE,
    IGNORE
}
