package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.CommandBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class FakeCommandBus implements CommandBus {
    private final List<Command<?>> dispatchedCommands = new ArrayList<>();
    private final Map<Class<?>, Function<Command<?>, Object>> handlers = new ConcurrentHashMap<>();

    public <C extends Command<R>, R> FakeCommandBus whenDispatching(Class<C> commandType, Function<C, R> handler) {
        Objects.requireNonNull(commandType, "commandType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        handlers.put(commandType, command -> handler.apply(commandType.cast(command)));
        return this;
    }

    public <C extends Command<R>, R> FakeCommandBus whenDispatchingReturn(Class<C> commandType, R result) {
        return whenDispatching(commandType, command -> result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <R> R dispatch(Command<R> command) {
        Objects.requireNonNull(command, "command must not be null");
        dispatchedCommands.add(command);
        Function<Command<?>, Object> handler = handlers.get(command.getClass());
        if (handler == null) {
            return null;
        }
        return (R) handler.apply(command);
    }

    public synchronized List<Command<?>> dispatchedCommands() {
        return List.copyOf(dispatchedCommands);
    }

    public synchronized <C extends Command<?>> List<C> dispatchedCommandsOfType(Class<C> commandType) {
        return dispatchedCommands.stream()
            .filter(commandType::isInstance)
            .map(commandType::cast)
            .toList();
    }

    public synchronized void clear() {
        dispatchedCommands.clear();
        handlers.clear();
    }
}
