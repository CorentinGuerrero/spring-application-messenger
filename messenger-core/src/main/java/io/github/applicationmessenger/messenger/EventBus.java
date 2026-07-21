package io.github.applicationmessenger.messenger;

import io.github.applicationmessenger.messenger.api.PublicApi;

@PublicApi
public interface EventBus {
    void publish(Event event);
}
