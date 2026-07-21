package io.github.applicationmessenger.messenger.dispatch;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.envelope.MessageMetadata;
import io.github.applicationmessenger.messenger.exception.EventHandlingException;
import io.github.applicationmessenger.messenger.handler.HandlerRegistry;
import io.github.applicationmessenger.messenger.middleware.MessageMiddleware;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class IncomingMessageDispatcher {
    private final HandlerRegistry registry;
    private final List<MessageMiddleware> middlewares;
    private final EventErrorStrategy eventErrorStrategy;

    public IncomingMessageDispatcher(HandlerRegistry registry, List<MessageMiddleware> middlewares, EventErrorStrategy eventErrorStrategy) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.middlewares = List.copyOf(middlewares);
        this.eventErrorStrategy = Objects.requireNonNull(eventErrorStrategy, "eventErrorStrategy must not be null");
    }

    public Object dispatch(BusType busType, Object payload, MessageMetadata metadata) {
        Objects.requireNonNull(busType, "busType must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        MessageEnvelope envelope = new MessageEnvelope(payload, metadata);
        return switch (busType) {
            case COMMAND -> new MiddlewareChain(middlewares, terminalEnvelope -> registry.commandHandler(payload.getClass()).handle(terminalEnvelope)).invoke(envelope);
            case QUERY -> new MiddlewareChain(middlewares, terminalEnvelope -> registry.queryHandler(payload.getClass()).handle(terminalEnvelope)).invoke(envelope);
            case EVENT -> new MiddlewareChain(middlewares, this::dispatchEvent).invoke(envelope);
        };
    }

    private Object dispatchEvent(MessageEnvelope envelope) {
        Object event = envelope.payload();
        List<Throwable> failures = new ArrayList<>();
        for (var handler : registry.eventHandlers(event.getClass())) {
            try {
                handler.handle(envelope);
            } catch (Throwable failure) {
                if (eventErrorStrategy == EventErrorStrategy.IGNORE) {
                    continue;
                }
                if (eventErrorStrategy == EventErrorStrategy.FAIL_FAST) {
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
