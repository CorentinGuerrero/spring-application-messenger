package io.github.project.messenger;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public interface EventBus {
    void publish(Event event);
}
