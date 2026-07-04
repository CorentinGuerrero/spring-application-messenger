package io.github.project.messenger;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public interface CommandBus {
    <R> R dispatch(Command<R> command);
}
