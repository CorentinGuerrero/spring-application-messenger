package io.github.corentinguerrero.messenger.dispatch;

import io.github.corentinguerrero.messenger.Command;
import io.github.corentinguerrero.messenger.CommandBus;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.HandlerRegistry;
import io.github.corentinguerrero.messenger.middleware.MessageMiddleware;
import io.github.corentinguerrero.messenger.routing.DefaultMessageRouter;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.routing.MessageRouter;
import io.github.corentinguerrero.messenger.transport.MessageTransportRegistry;

import java.util.List;
import java.util.Objects;

public final class DefaultCommandBus implements CommandBus {
    private final HandlerRegistry registry;
    private final List<MessageMiddleware> middlewares;
    private final MessageRouter router;
    private final MessageTransportRegistry transportRegistry;

    public DefaultCommandBus(HandlerRegistry registry, List<MessageMiddleware> middlewares) {
        this(registry, middlewares, DefaultMessageRouter.syncOnly(), MessageTransportRegistry.syncOnly());
    }

    public DefaultCommandBus(HandlerRegistry registry, List<MessageMiddleware> middlewares, MessageRouter router, MessageTransportRegistry transportRegistry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.middlewares = List.copyOf(middlewares);
        this.router = Objects.requireNonNull(router, "router must not be null");
        this.transportRegistry = Objects.requireNonNull(transportRegistry, "transportRegistry must not be null");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Command<R> command) {
        Objects.requireNonNull(command, "command must not be null");
        MessageEnvelope envelope = MessageEnvelope.of(command);
        MessageRoute route = router.routeFor(command, BusType.COMMAND);
        MiddlewareChain chain = new MiddlewareChain(middlewares, terminalEnvelope ->
            transportRegistry.dispatch(terminalEnvelope, route, routedEnvelope -> registry.commandHandler(command.getClass()).handle(routedEnvelope)));
        return (R) chain.invoke(envelope);
    }
}
