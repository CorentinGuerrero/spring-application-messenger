package io.github.corentinguerrero.messenger.test;

import io.github.corentinguerrero.messenger.Query;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FakeQueryBusTest {
    @Test
    void recordsAskedQueriesAndReturnsConfiguredResult() {
        FakeQueryBus bus = new FakeQueryBus()
            .whenAskingReturn(GetUser.class, new UserView("42"));

        UserView result = bus.ask(new GetUser("ignored"));

        assertEquals(new UserView("42"), result);
        assertEquals(1, bus.askedQueries().size());
    }

    @Test
    void recordsAskedQueriesAndUsesHandler() {
        FakeQueryBus bus = new FakeQueryBus()
            .whenAsking(GetUser.class, query -> new UserView(query.userId()));

        UserView result = bus.ask(new GetUser("42"));

        assertEquals(new UserView("42"), result);
        assertEquals(1, bus.askedQueries().size());
        assertEquals(new GetUser("42"), bus.askedQueriesOfType(GetUser.class).get(0));
    }

    @Test
    void returnsNullWhenNoResultIsConfigured() {
        FakeQueryBus bus = new FakeQueryBus();

        UserView result = bus.ask(new GetUser("42"));

        assertNull(result);
        assertEquals(1, bus.askedQueries().size());
    }

    record GetUser(String userId) implements Query<UserView> {
    }

    record UserView(String userId) {
    }
}
