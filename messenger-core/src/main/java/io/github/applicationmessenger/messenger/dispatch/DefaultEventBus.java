package io.github.applicationmessenger.messenger.dispatch;

import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.EventBus;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.exception.EventHandlingException;
import io.github.applicationmessenger.messenger.handler.HandlerRegistry;
import io.github.applicationmessenger.messenger.middleware.MessageMiddleware;
import io.github.applicationmessenger.messenger.routing.DefaultMessageRouter;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.routing.MessageRouter;
import io.github.applicationmessenger.messenger.transport.MessageTransportRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultEventBus implements EventBus {
    private final HandlerRegistry registry;
    private final List<MessageMiddleware> middlewares;
    private final EventErrorStrategy errorStrategy;
    private final MessageRouter router;
    private final MessageTransportRegistry transportRegistry;

    public DefaultEventBus(HandlerRegistry registry, List<MessageMiddleware> middlewares, EventErrorStrategy errorStrategy) {
        this(registry, middlewares, errorStrategy, DefaultMessageRouter.syncOnly(), MessageTransportRegistry.syncOnly());
    }

    public DefaultEventBus(HandlerRegistry registry, List<MessageMiddleware> middlewares, EventErrorStrategy errorStrategy, MessageRouter router, MessageTransportRegistry transportRegistry) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.middlewares = List.copyOf(middlewares);
        this.errorStrategy = Objects.requireNonNull(errorStrategy, "errorStrategy must not be null");
        this.router = Objects.requireNonNull(router, "router must not be null");
        this.transportRegistry = Objects.requireNonNull(transportRegistry, "transportRegistry must not be null");
    }

    @Override
    public void publish(Event event) {
        Objects.requireNonNull(event, "event must not be null");
        MessageEnvelope envelope = MessageEnvelope.of(event);
        MessageRoute route = router.routeFor(event, BusType.EVENT);
        MiddlewareChain chain = new MiddlewareChain(middlewares, terminalEnvelope ->
            transportRegistry.dispatch(terminalEnvelope, route, this::invokeLocalEventHandlers));
        chain.invoke(envelope);
    }

    private Object invokeLocalEventHandlers(MessageEnvelope envelope) {
        Object event = envelope.payload();
        List<Throwable> failures = new ArrayList<>();

        for (var handler : registry.eventHandlers(event.getClass())) {
            try {
                handler.handle(envelope);
            } catch (Throwable failure) {
                if (errorStrategy == EventErrorStrategy.IGNORE) {
                    continue;
                }
                if (errorStrategy == EventErrorStrategy.FAIL_FAST) {
                    throwUnchecked(failure);
                }
                failures.add(failure);
            }
        }

        if (!failures.isEmpty()) {
            throw new EventHandlingException(event.getClass(), failures);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwUnchecked(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
