package io.github.applicationmessenger.messenger.test;

public final class MessengerAssertions {
    private MessengerAssertions() {
    }

    public static EventRecorderAssert assertThat(EventRecorder recorder) {
        return new EventRecorderAssert(recorder);
    }

    public static EventRecorderAssert assertThat(RecordingEventBus eventBus) {
        return new EventRecorderAssert(eventBus.recorder());
    }
}
