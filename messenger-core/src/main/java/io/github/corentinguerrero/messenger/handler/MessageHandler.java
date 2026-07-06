package io.github.corentinguerrero.messenger.handler;

import io.github.corentinguerrero.messenger.api.Spi;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;

@FunctionalInterface
@Spi
public interface MessageHandler {
    Object handle(MessageEnvelope envelope);
}
