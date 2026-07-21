package io.github.applicationmessenger.messenger.api;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.EventBus;
import io.github.applicationmessenger.messenger.Query;
import io.github.applicationmessenger.messenger.QueryBus;
import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.dispatch.EventErrorStrategy;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.envelope.MessageMetadata;
import io.github.applicationmessenger.messenger.handler.HandlerRegistry;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.middleware.MessageMiddleware;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.routing.MessageRouter;
import io.github.applicationmessenger.messenger.transport.MessageTransport;
import io.github.applicationmessenger.messenger.transport.TransportMessage;
import io.github.applicationmessenger.messenger.transport.TransportNames;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessengerApiContractTest {
    @Test
    void busContractsStayStable() throws Exception {
        Method dispatch = CommandBus.class.getMethod("dispatch", Command.class);
        Method ask = QueryBus.class.getMethod("ask", Query.class);
        Method publish = EventBus.class.getMethod("publish", Event.class);

        assertEquals(Object.class, dispatch.getReturnType());
        assertEquals(Object.class, ask.getReturnType());
        assertEquals(Void.TYPE, publish.getReturnType());
    }

    @Test
    void messageMetadataContractStaysStable() throws Exception {
        assertDoesNotThrow(() -> MessageMetadata.class.getConstructor(UUID.class, UUID.class, UUID.class, Instant.class, Map.class));
        assertNotNull(MessageMetadata.class.getMethod("create"));
        assertEquals(MessageMetadata.class, MessageMetadata.class.getMethod("withHeader", String.class, Object.class).getReturnType());

        assertEquals(UUID.class, MessageMetadata.class.getMethod("messageId").getReturnType());
        assertEquals(UUID.class, MessageMetadata.class.getMethod("correlationId").getReturnType());
        assertEquals(UUID.class, MessageMetadata.class.getMethod("causationId").getReturnType());
        assertEquals(Instant.class, MessageMetadata.class.getMethod("createdAt").getReturnType());
        assertEquals(Map.class, MessageMetadata.class.getMethod("headers").getReturnType());
    }

    @Test
    void envelopeAndMiddlewareContractsStayStable() throws Exception {
        assertDoesNotThrow(() -> MessageEnvelope.class.getConstructor(Object.class, MessageMetadata.class));
        assertEquals(MessageEnvelope.class, MessageEnvelope.class.getMethod("of", Object.class).getReturnType());
        assertEquals(Object.class, MessageEnvelope.class.getMethod("payload").getReturnType());
        assertEquals(MessageMetadata.class, MessageEnvelope.class.getMethod("metadata").getReturnType());

        assertEquals(Object.class, MessageMiddleware.class.getMethod("invoke", MessageEnvelope.class, MessageHandler.class).getReturnType());
        assertEquals(Object.class, MessageHandler.class.getMethod("handle", MessageEnvelope.class).getReturnType());
    }

    @Test
    void spiContractsStayStable() throws Exception {
        assertEquals(MessageHandler.class, HandlerRegistry.class.getMethod("commandHandler", Class.class).getReturnType());
        assertEquals(MessageHandler.class, HandlerRegistry.class.getMethod("queryHandler", Class.class).getReturnType());
        assertEquals(List.class, HandlerRegistry.class.getMethod("eventHandlers", Class.class).getReturnType());

        assertEquals(MessageRoute.class, MessageRouter.class.getMethod("routeFor", Object.class, BusType.class).getReturnType());
        assertEquals(String.class, MessageTransport.class.getMethod("name").getReturnType());
        assertEquals(Object.class, MessageTransport.class.getMethod("dispatch", MessageEnvelope.class, MessageRoute.class, MessageHandler.class).getReturnType());
        assertArrayEquals(
            new String[]{"messageType", "busType", "transportName", "payload", "metadata"},
            Arrays.stream(TransportMessage.class.getRecordComponents()).map(component -> component.getName()).toArray(String[]::new)
        );
    }

    @Test
    void stableEnumValuesAndTransportNamesDoNotDrift() {
        assertArrayEquals(new String[]{"COMMAND", "QUERY", "EVENT"}, enumNames(BusType.values()));
        assertArrayEquals(new String[]{"FAIL_FAST", "CONTINUE", "IGNORE"}, enumNames(EventErrorStrategy.values()));

        assertEquals("sync", TransportNames.SYNC);
        assertEquals("rabbitmq", TransportNames.RABBITMQ);
        assertEquals("kafka", TransportNames.KAFKA);
        assertEquals("redis", TransportNames.REDIS);
        assertEquals("pg-outbox", TransportNames.JDBC_OUTBOX);
        assertEquals("pg", TransportNames.PG_OUTBOX_ALIAS);
    }

    @Test
    void stableTypesAreMarkedAsPublicApiOrSpi() {
        assertTrue(Command.class.isAnnotationPresent(PublicApi.class));
        assertTrue(Query.class.isAnnotationPresent(PublicApi.class));
        assertTrue(Event.class.isAnnotationPresent(PublicApi.class));
        assertTrue(CommandBus.class.isAnnotationPresent(PublicApi.class));
        assertTrue(QueryBus.class.isAnnotationPresent(PublicApi.class));
        assertTrue(EventBus.class.isAnnotationPresent(PublicApi.class));
        assertTrue(MessageEnvelope.class.isAnnotationPresent(PublicApi.class));
        assertTrue(MessageMetadata.class.isAnnotationPresent(PublicApi.class));
        assertTrue(MessageMiddleware.class.isAnnotationPresent(PublicApi.class));
        assertTrue(MessageTransport.class.isAnnotationPresent(Spi.class));
        assertTrue(MessageRouter.class.isAnnotationPresent(Spi.class));
        assertTrue(HandlerRegistry.class.isAnnotationPresent(Spi.class));
    }

    private static String[] enumNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toArray(String[]::new);
    }
}
