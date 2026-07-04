package io.github.project.messenger;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public interface QueryBus {
    <R> R ask(Query<R> query);
}
