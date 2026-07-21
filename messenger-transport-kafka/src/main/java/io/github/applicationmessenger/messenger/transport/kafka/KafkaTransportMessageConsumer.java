package io.github.applicationmessenger.messenger.transport.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.applicationmessenger.messenger.dispatch.IncomingMessageDispatcher;
import io.github.applicationmessenger.messenger.exception.MessageDispatchException;
import io.github.applicationmessenger.messenger.transport.TransportMessage;
import org.springframework.kafka.annotation.KafkaListener;

public final class KafkaTransportMessageConsumer {
    private final ObjectMapper objectMapper;
    private final IncomingMessageDispatcher dispatcher;

    public KafkaTransportMessageConsumer(ObjectMapper objectMapper, IncomingMessageDispatcher dispatcher) {
        this.objectMapper = objectMapper.findAndRegisterModules();
        this.dispatcher = dispatcher;
    }

    @KafkaListener(
        topics = "#{'${messenger.transports.kafka.consumer.topics:}'.split(',')}",
        groupId = "${messenger.transports.kafka.consumer.group-id:messenger}",
        autoStartup = "${messenger.transports.kafka.consumer.enabled:false}"
    )
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
