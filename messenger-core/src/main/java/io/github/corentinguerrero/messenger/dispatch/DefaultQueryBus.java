package io.github.corentinguerrero.messenger.dispatch;

import io.github.corentinguerrero.messenger.Query;
import io.github.corentinguerrero.messenger.QueryBus;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.HandlerRegistry;
import io.github.corentinguerrero.messenger.middleware.MessageMiddleware;

import java.util.List;
import java.util.Objects;

public final class DefaultQueryBus implements QueryBus {
    private final HandlerRegistry registry;
    private final List<MessageMiddleware> middlewares;

    public DefaultQueryBus(HandlerRegistry registry, List<MessageMiddleware> middlewares) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.middlewares = List.copyOf(middlewares);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R ask(Query<R> query) {
        Objects.requireNonNull(query, "query must not be null");
        MessageEnvelope envelope = MessageEnvelope.of(query);
        MiddlewareChain chain = new MiddlewareChain(middlewares, terminalEnvelope ->
            registry.queryHandler(query.getClass()).handle(terminalEnvelope));
        return (R) chain.invoke(envelope);
    }
}
