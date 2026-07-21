package io.github.applicationmessenger.messenger.handler;

import io.github.applicationmessenger.messenger.api.Spi;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;

@FunctionalInterface
@Spi
public interface MessageHandler {
    Object handle(MessageEnvelope envelope);
}
