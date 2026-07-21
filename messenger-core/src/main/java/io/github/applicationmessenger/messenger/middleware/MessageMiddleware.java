package io.github.applicationmessenger.messenger.middleware;

import io.github.applicationmessenger.messenger.api.PublicApi;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.handler.MessageHandler;

@FunctionalInterface
@PublicApi
public interface MessageMiddleware {
    Object invoke(MessageEnvelope envelope, MessageHandler next);
}
