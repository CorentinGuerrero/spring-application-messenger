package io.github.project.messenger.dispatch;

import io.github.project.messenger.Command;
import io.github.project.messenger.CommandBus;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.handler.HandlerRegistry;
import io.github.project.messenger.middleware.MessageMiddleware;
import io.github.project.messenger.routing.DefaultMessageRouter;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.routing.MessageRouter;
import io.github.project.messenger.transport.MessageTransportRegistry;

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
