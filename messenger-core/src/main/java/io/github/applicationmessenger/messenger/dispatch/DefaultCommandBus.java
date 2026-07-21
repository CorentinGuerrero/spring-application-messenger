package io.github.applicationmessenger.messenger.dispatch;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.handler.HandlerRegistry;
import io.github.applicationmessenger.messenger.middleware.MessageMiddleware;
import io.github.applicationmessenger.messenger.routing.DefaultMessageRouter;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.routing.MessageRouter;
import io.github.applicationmessenger.messenger.transport.MessageTransportRegistry;

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
