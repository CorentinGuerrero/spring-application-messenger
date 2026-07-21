package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FakeCommandBusTest {
    @Test
    void recordsDispatchedCommandsAndReturnsConfiguredResult() {
        FakeCommandBus bus = new FakeCommandBus()
            .whenDispatching(RegisterUser.class, command -> new UserId(command.email()));

        UserId result = bus.dispatch(new RegisterUser("john@example.com"));

        assertEquals(new UserId("john@example.com"), result);
        assertEquals(1, bus.dispatchedCommands().size());
        assertEquals(new RegisterUser("john@example.com"), bus.dispatchedCommandsOfType(RegisterUser.class).get(0));
    }

    @Test
    void returnsNullWhenNoResultIsConfigured() {
        FakeCommandBus bus = new FakeCommandBus();

        UserId result = bus.dispatch(new RegisterUser("john@example.com"));

        assertNull(result);
        assertEquals(1, bus.dispatchedCommands().size());
    }

    record RegisterUser(String email) implements Command<UserId> {
    }

    record UserId(String value) {
    }
}
