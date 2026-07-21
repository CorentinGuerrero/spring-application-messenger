package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EventRecorder {
    private final List<Event> events = new ArrayList<>();

    public synchronized void record(Event event) {
        events.add(Objects.requireNonNull(event, "event must not be null"));
    }

    public synchronized List<Event> events() {
        return List.copyOf(events);
    }

    public synchronized <E extends Event> List<E> eventsOfType(Class<E> eventType) {
        return events.stream()
            .filter(eventType::isInstance)
            .map(eventType::cast)
            .toList();
    }

    public synchronized boolean hasPublished(Class<? extends Event> eventType) {
        return events.stream().anyMatch(eventType::isInstance);
    }

    public synchronized int count(Class<? extends Event> eventType) {
        return (int) events.stream().filter(eventType::isInstance).count();
    }

    public synchronized void clear() {
        events.clear();
    }
}
