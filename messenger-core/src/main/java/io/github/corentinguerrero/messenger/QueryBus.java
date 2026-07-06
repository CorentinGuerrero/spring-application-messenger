package io.github.corentinguerrero.messenger;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public interface QueryBus {
    <R> R ask(Query<R> query);
}
