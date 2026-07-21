package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.Query;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HandlerTestSupportTest {
    @Test
    void invokesCommandHandlerDirectly() {
        UserId result = HandlerTestSupport.invokeCommand(new RegisterUserHandler(), new RegisterUser("john@example.com"));

        assertEquals(new UserId("john@example.com"), result);
    }

    @Test
    void invokesQueryHandlerDirectly() {
        UserView result = HandlerTestSupport.invokeQuery(new GetUserHandler(), new GetUser("42"));

        assertEquals(new UserView("42"), result);
    }

    @Test
    void invokesEventHandlerDirectly() {
        WelcomeEmailHandler handler = new WelcomeEmailHandler();

        HandlerTestSupport.invokeEvent(handler, new UserRegistered("john@example.com"));

        assertEquals("john@example.com", handler.lastEmail);
    }

    @Test
    void rethrowsBusinessExceptions() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> HandlerTestSupport.invokeCommand(new FailingHandler(), new RegisterUser("john@example.com")));

        assertEquals("boom", exception.getMessage());
    }

    record RegisterUser(String email) implements Command<UserId> {
    }

    record UserId(String value) {
    }

    record GetUser(String userId) implements Query<UserView> {
    }

    record UserView(String userId) {
    }

    record UserRegistered(String email) implements Event {
    }

    static final class RegisterUserHandler {
        UserId handle(RegisterUser command) {
            return new UserId(command.email());
        }
    }

    static final class GetUserHandler {
        UserView handle(GetUser query) {
            return new UserView(query.userId());
        }
    }

    static final class WelcomeEmailHandler {
        private String lastEmail;

        void handle(UserRegistered event) {
            lastEmail = event.email();
        }
    }

    static final class FailingHandler {
        UserId handle(RegisterUser command) {
            throw new IllegalStateException("boom");
        }
    }
}
