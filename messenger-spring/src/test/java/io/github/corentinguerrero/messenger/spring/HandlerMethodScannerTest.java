package io.github.corentinguerrero.messenger.spring;

import io.github.corentinguerrero.messenger.Command;
import io.github.corentinguerrero.messenger.Event;
import io.github.corentinguerrero.messenger.Query;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.exception.InvalidHandlerSignatureException;
import io.github.corentinguerrero.messenger.handler.HandlerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandlerMethodScannerTest {
    @Test
    void registersAnnotatedHandlersFromSpringContext() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(CreateGreetingHandler.class, GetGreetingHandler.class, GreetingCreatedHandler.class);
            context.refresh();

            HandlerRegistry registry = new HandlerMethodScanner(context).scan();

            assertThat(registry.commandHandler(CreateGreeting.class).handle(MessageEnvelope.of(new CreateGreeting("Ada"))))
                .isEqualTo("created Ada");
            assertThat(registry.queryHandler(GetGreeting.class).handle(MessageEnvelope.of(new GetGreeting("Grace"))))
                .isEqualTo("hello Grace");
            assertThat(registry.eventHandlers(GreetingCreated.class)).hasSize(1);
        }
    }

    @Test
    void rejectsInvalidHandlerSignatures() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(InvalidGreetingHandler.class);
            context.refresh();

            assertThatThrownBy(() -> new HandlerMethodScanner(context).scan())
                .isInstanceOf(InvalidHandlerSignatureException.class)
                .hasMessageContaining("exactly one message parameter");
        }
    }

    @Test
    void rejectsIncompatibleReturnTypes() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(IncompatibleGreetingHandler.class);
            context.refresh();

            assertThatThrownBy(() -> new HandlerMethodScanner(context).scan())
                .isInstanceOf(InvalidHandlerSignatureException.class)
                .hasMessageContaining("return type");
        }
    }

    record CreateGreeting(String name) implements Command<String> {
    }

    record GetGreeting(String name) implements Query<String> {
    }

    record GreetingCreated(String name) implements Event {
    }

    @CommandHandler
    static class CreateGreetingHandler {
        String handle(CreateGreeting command) {
            return "created " + command.name();
        }
    }

    @QueryHandler
    static class GetGreetingHandler {
        String handle(GetGreeting query) {
            return "hello " + query.name();
        }
    }

    @EventHandler
    static class GreetingCreatedHandler {
        final List<GreetingCreated> events = new ArrayList<>();

        void handle(GreetingCreated event) {
            events.add(event);
        }
    }

    @CommandHandler
    static class InvalidGreetingHandler {
        String handle() {
            return "nope";
        }
    }

    @CommandHandler
    static class IncompatibleGreetingHandler {
        Integer handle(CreateGreeting command) {
            return command.name().length();
        }
    }
}
