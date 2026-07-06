package io.github.corentinguerrero.messenger.test;

import io.github.corentinguerrero.messenger.Event;
import io.github.corentinguerrero.messenger.EventBus;

import java.util.Objects;

public final class RecordingEventBus implements EventBus {
    private final EventRecorder recorder;

    public RecordingEventBus() {
        this(new EventRecorder());
    }

    public RecordingEventBus(EventRecorder recorder) {
        this.recorder = Objects.requireNonNull(recorder, "recorder must not be null");
    }

    @Override
    public void publish(Event event) {
        recorder.record(event);
    }

    public EventRecorder recorder() {
        return recorder;
    }
}
