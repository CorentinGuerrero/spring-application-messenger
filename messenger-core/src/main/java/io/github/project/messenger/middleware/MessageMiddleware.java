package io.github.project.messenger.middleware;

import io.github.project.messenger.api.PublicApi;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.handler.MessageHandler;

@FunctionalInterface
@PublicApi
public interface MessageMiddleware {
    Object invoke(MessageEnvelope envelope, MessageHandler next);
}
