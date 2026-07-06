package io.github.corentinguerrero.messenger.dispatch;

import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.MessageHandler;
import io.github.corentinguerrero.messenger.middleware.MessageMiddleware;

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
