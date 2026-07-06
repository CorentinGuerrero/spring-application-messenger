package io.github.corentinguerrero.messenger;

import io.github.corentinguerrero.messenger.api.PublicApi;

@PublicApi
public interface CommandBus {
    <R> R dispatch(Command<R> command);
}
