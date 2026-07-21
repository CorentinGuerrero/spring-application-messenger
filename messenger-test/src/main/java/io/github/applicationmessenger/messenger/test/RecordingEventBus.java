package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.EventBus;

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
