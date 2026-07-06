package io.github.corentinguerrero.messenger.middleware;

import io.github.corentinguerrero.messenger.api.PublicApi;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.MessageHandler;

@FunctionalInterface
@PublicApi
public interface MessageMiddleware {
    Object invoke(MessageEnvelope envelope, MessageHandler next);
}
