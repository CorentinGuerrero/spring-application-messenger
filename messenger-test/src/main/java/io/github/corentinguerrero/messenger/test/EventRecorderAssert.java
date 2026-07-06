package io.github.corentinguerrero.messenger.test;

import io.github.corentinguerrero.messenger.Event;

import java.util.Objects;
import java.util.function.Consumer;

public final class EventRecorderAssert {
    private final EventRecorder recorder;

    EventRecorderAssert(EventRecorder recorder) {
        this.recorder = Objects.requireNonNull(recorder, "recorder must not be null");
    }

    public EventRecorderAssert hasPublished(Class<? extends Event> eventType) {
        if (!recorder.hasPublished(eventType)) {
            throw new AssertionError("Expected event " + eventType.getName() + " to have been published");
        }
        return this;
    }

    public EventRecorderAssert hasNotPublished(Class<? extends Event> eventType) {
        if (recorder.hasPublished(eventType)) {
            throw new AssertionError("Expected event " + eventType.getName() + " not to have been published");
        }
        return this;
    }

    public EventRecorderAssert hasPublishedCount(Class<? extends Event> eventType, int expectedCount) {
        int actualCount = recorder.count(eventType);
        if (actualCount != expectedCount) {
            throw new AssertionError("Expected event " + eventType.getName()
                + " to have been published " + expectedCount + " time(s), but was " + actualCount);
        }
        return this;
    }

    public <E extends Event> EventRecorderAssert hasPublishedSatisfying(Class<E> eventType, Consumer<E> assertion) {
        Objects.requireNonNull(assertion, "assertion must not be null");
        E event = recorder.eventsOfType(eventType).stream()
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected event " + eventType.getName() + " to have been published"));
        assertion.accept(event);
        return this;
    }
}
