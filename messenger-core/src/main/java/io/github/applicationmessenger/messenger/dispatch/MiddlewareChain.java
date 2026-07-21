package io.github.applicationmessenger.messenger.dispatch;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.middleware.MessageMiddleware;

import java.util.List;

public final class MiddlewareChain {
    private final List<MessageMiddleware> middlewares;
    private final MessageHandler terminalHandler;

    public MiddlewareChain(List<MessageMiddleware> middlewares, MessageHandler terminalHandler) {
        this.middlewares = middlewares;
        this.terminalHandler = terminalHandler;
    }

    public Object invoke(MessageEnvelope envelope) {
        MessageHandler chain = terminalHandler;
        for (int i = middlewares.size() - 1; i >= 0; i--) {
            MessageMiddleware middleware = middlewares.get(i);
            MessageHandler next = chain;
            chain = currentEnvelope -> middleware.invoke(currentEnvelope, next);
        }
        return chain.handle(envelope);
    }
}
