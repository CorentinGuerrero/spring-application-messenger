package io.github.corentinguerrero.messenger.test;

import io.github.corentinguerrero.messenger.Event;
import org.junit.jupiter.api.Test;

import static io.github.corentinguerrero.messenger.test.MessengerAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventRecorderTest {
    @Test
    void recordsEventsThroughRecordingEventBus() {
        RecordingEventBus eventBus = new RecordingEventBus();

        eventBus.publish(new UserRegistered("42"));
        eventBus.publish(new UserDeleted("42"));

        assertEquals(2, eventBus.recorder().events().size());
        assertEquals(1, eventBus.recorder().eventsOfType(UserRegistered.class).size());
        assertThat(eventBus)
            .hasPublished(UserRegistered.class)
            .hasPublished(UserDeleted.class)
            .hasPublishedCount(UserRegistered.class, 1)
            .hasPublishedSatisfying(UserRegistered.class, event -> assertEquals("42", event.userId()));
    }

    @Test
    void assertionsFailWithAssertionError() {
        EventRecorder recorder = new EventRecorder();

        AssertionError error = assertThrows(AssertionError.class,
            () -> assertThat(recorder).hasPublished(UserRegistered.class));

        assertEquals("Expected event " + UserRegistered.class.getName() + " to have been published", error.getMessage());
    }

    record UserRegistered(String userId) implements Event {
    }

    record UserDeleted(String userId) implements Event {
    }
}
