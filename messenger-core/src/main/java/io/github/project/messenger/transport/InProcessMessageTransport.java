package io.github.project.messenger.transport;

import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.handler.MessageHandler;
import io.github.project.messenger.routing.MessageRoute;

public final class InProcessMessageTransport implements MessageTransport {
    @Override
    public String name() {
        return TransportNames.SYNC;
    }

    @Override
    public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
        return next.handle(envelope);
    }
}
