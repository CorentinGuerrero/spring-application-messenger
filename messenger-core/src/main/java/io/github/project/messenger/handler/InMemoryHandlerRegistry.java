package io.github.project.messenger.handler;

import io.github.project.messenger.exception.MultipleHandlersFoundException;
import io.github.project.messenger.exception.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryHandlerRegistry implements HandlerRegistry {
    private final Map<Class<?>, MessageHandler> commandHandlers;
    private final Map<Class<?>, MessageHandler> queryHandlers;
    private final Map<Class<?>, List<MessageHandler>> eventHandlers;

    private InMemoryHandlerRegistry(Builder builder) {
        this.commandHandlers = Map.copyOf(builder.commandHandlers);
        this.queryHandlers = Map.copyOf(builder.queryHandlers);
        this.eventHandlers = copyEvents(builder.eventHandlers);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public MessageHandler commandHandler(Class<?> commandType) {
        MessageHandler handler = commandHandlers.get(commandType);
        if (handler == null) {
            throw new NoHandlerFoundException(commandType);
        }
        return handler;
    }

    @Override
    public MessageHandler queryHandler(Class<?> queryType) {
        MessageHandler handler = queryHandlers.get(queryType);
        if (handler == null) {
            throw new NoHandlerFoundException(queryType);
        }
        return handler;
    }

    @Override
    public List<MessageHandler> eventHandlers(Class<?> eventType) {
        return eventHandlers.getOrDefault(eventType, List.of());
    }

    private static Map<Class<?>, List<MessageHandler>> copyEvents(Map<Class<?>, List<MessageHandler>> source) {
        Map<Class<?>, List<MessageHandler>> copy = new LinkedHashMap<>();
        source.forEach((type, handlers) -> copy.put(type, List.copyOf(handlers)));
        return Map.copyOf(copy);
    }

    public static final class Builder {
        private final Map<Class<?>, MessageHandler> commandHandlers = new LinkedHashMap<>();
        private final Map<Class<?>, MessageHandler> queryHandlers = new LinkedHashMap<>();
        private final Map<Class<?>, List<MessageHandler>> eventHandlers = new LinkedHashMap<>();

        public Builder commandHandler(Class<?> commandType, MessageHandler handler) {
            if (commandHandlers.containsKey(commandType)) {
                throw new MultipleHandlersFoundException(commandType);
            }
            commandHandlers.put(commandType, handler);
            return this;
        }

        public Builder queryHandler(Class<?> queryType, MessageHandler handler) {
            if (queryHandlers.containsKey(queryType)) {
                throw new MultipleHandlersFoundException(queryType);
            }
            queryHandlers.put(queryType, handler);
            return this;
        }

        public Builder eventHandler(Class<?> eventType, MessageHandler handler) {
            eventHandlers.computeIfAbsent(eventType, ignored -> new ArrayList<>()).add(handler);
            return this;
        }

        public InMemoryHandlerRegistry build() {
            return new InMemoryHandlerRegistry(this);
        }
    }
}
