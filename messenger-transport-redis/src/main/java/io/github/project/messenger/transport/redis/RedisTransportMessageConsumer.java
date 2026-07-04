package io.github.project.messenger.transport.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.messenger.dispatch.IncomingMessageDispatcher;
import io.github.project.messenger.exception.MessageDispatchException;
import io.github.project.messenger.transport.TransportMessage;

public final class RedisTransportMessageConsumer {
    private final ObjectMapper objectMapper;
    private final IncomingMessageDispatcher dispatcher;

    public RedisTransportMessageConsumer(ObjectMapper objectMapper, IncomingMessageDispatcher dispatcher) {
        this.objectMapper = objectMapper.findAndRegisterModules();
        this.dispatcher = dispatcher;
    }

    public void consume(TransportMessage message) {
        dispatcher.dispatch(message.busType(), payload(message), message.metadata());
    }

    private Object payload(TransportMessage message) {
        try {
            return objectMapper.convertValue(message.payload(), Class.forName(message.messageType()));
        } catch (ClassNotFoundException exception) {
            throw new MessageDispatchException("Could not resolve message type " + message.messageType(), exception);
        }
    }
}
