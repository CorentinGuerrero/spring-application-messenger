package io.github.project.messenger.exception;

import io.github.project.messenger.api.PublicApi;

import java.util.List;

@PublicApi
public final class EventHandlingException extends RuntimeException {
    public EventHandlingException(Class<?> eventType, List<Throwable> failures) {
        super("Event " + eventType.getName() + " failed in " + failures.size() + " handler(s)", failures.get(0));
        failures.forEach(this::addSuppressed);
    }
}
