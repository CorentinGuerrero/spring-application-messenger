package io.github.project.messenger.handler;

import io.github.project.messenger.api.Spi;
import io.github.project.messenger.envelope.MessageEnvelope;

@FunctionalInterface
@Spi
public interface MessageHandler {
    Object handle(MessageEnvelope envelope);
}
