package io.github.applicationmessenger.messenger;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public interface QueryBus {
    <R> R ask(Query<R> query);
}
