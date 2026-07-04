package io.github.project.messenger.dispatch;

import io.github.project.messenger.Command;
import io.github.project.messenger.Event;
import io.github.project.messenger.Query;
import io.github.project.messenger.exception.EventHandlingException;
import io.github.project.messenger.handler.InMemoryHandlerRegistry;
import io.github.project.messenger.middleware.MessageMiddleware;
import io.github.project.messenger.routing.DefaultMessageRouter;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.transport.MessageTransport;
import io.github.project.messenger.transport.MessageTransportRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBusesTest {
    @Test
    void dispatchesCommandThroughMiddlewareChain() {
        List<String> calls = new ArrayList<>();
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .commandHandler(CreateGreeting.class, envelope -> {
                calls.add("handler");
                return "hello " + ((CreateGreeting) envelope.payload()).name();
            })
            .build();
        MessageMiddleware first = (envelope, next) -> {
            calls.add("first-before");
            Object result = next.handle(envelope);
            calls.add("first-after");
            return result;
        };
        MessageMiddleware second = (envelope, next) -> {
            calls.add("second-before");
            Object result = next.handle(envelope);
            calls.add("second-after");
            return result;
        };

        String result = new DefaultCommandBus(registry, List.of(first, second)).dispatch(new CreateGreeting("Ada"));

        assertEquals("hello Ada", result);
        assertEquals(List.of("first-before", "second-before", "handler", "second-after", "first-after"), calls);
    }

    @Test
    void asksQueryHandler() {
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .queryHandler(GetGreeting.class, envelope -> "hello " + ((GetGreeting) envelope.payload()).name())
            .build();

        String result = new DefaultQueryBus(registry, List.of()).ask(new GetGreeting("Grace"));

        assertEquals("hello Grace", result);
    }

    @Test
    void queriesAreAlwaysHandledSynchronouslyInProcess() {
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .queryHandler(GetGreeting.class, envelope -> "hello " + ((GetGreeting) envelope.payload()).name())
            .build();

        String result = new DefaultQueryBus(registry, List.of()).ask(new GetGreeting("Ada"));

        assertEquals("hello Ada", result);
    }

    @Test
    void publishesEventToAllHandlers() {
        List<String> calls = new ArrayList<>();
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .eventHandler(GreetingCreated.class, envelope -> {
                calls.add("one");
                return null;
            })
            .eventHandler(GreetingCreated.class, envelope -> {
                calls.add("two");
                return null;
            })
            .build();

        new DefaultEventBus(registry, List.of(), EventErrorStrategy.FAIL_FAST).publish(new GreetingCreated("Ada"));

        assertEquals(List.of("one", "two"), calls);
    }

    @Test
    void aggregatesEventFailuresWhenContinueStrategyIsUsed() {
        List<String> calls = new ArrayList<>();
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .eventHandler(GreetingCreated.class, envelope -> {
                calls.add("one");
                throw new IllegalStateException("boom");
            })
            .eventHandler(GreetingCreated.class, envelope -> {
                calls.add("two");
                return null;
            })
            .build();

        EventHandlingException exception = assertThrows(EventHandlingException.class,
            () -> new DefaultEventBus(registry, List.of(), EventErrorStrategy.CONTINUE).publish(new GreetingCreated("Ada")));

        assertEquals(List.of("one", "two"), calls);
        assertEquals(1, exception.getSuppressed().length);
    }

    @Test
    void dispatchesCommandToConfiguredTransport() {
        CapturingTransport transport = new CapturingTransport("test-transport", "remote-result");
        InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
            .commandHandler(CreateGreeting.class, envelope -> "local-result")
            .build();
        DefaultMessageRouter router = new DefaultMessageRouter("sync", Map.of("CreateGreeting", "test-transport"), Map.of());
        MessageTransportRegistry transportRegistry = new MessageTransportRegistry(List.of(transport));

        String result = new DefaultCommandBus(registry, List.of(), router, transportRegistry)
            .dispatch(new CreateGreeting("Ada"));

        assertEquals("remote-result", result);
        assertEquals(BusType.COMMAND, transport.route.busType());
        assertEquals(CreateGreeting.class, transport.route.messageType());
        assertEquals("test-transport", transport.route.transportName());
        assertEquals(false, transport.localHandlerInvoked);
    }

    record CreateGreeting(String name) implements Command<String> {
    }

    record GetGreeting(String name) implements Query<String> {
    }

    record GreetingCreated(String name) implements Event {
    }

    static final class CapturingTransport implements MessageTransport {
        private final String name;
        private final Object result;
        private MessageRoute route;
        private boolean localHandlerInvoked;

        CapturingTransport(String name, Object result) {
            this.name = name;
            this.result = result;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object dispatch(io.github.project.messenger.envelope.MessageEnvelope envelope, MessageRoute route, io.github.project.messenger.handler.MessageHandler next) {
            this.route = route;
            return result;
        }
    }
}
