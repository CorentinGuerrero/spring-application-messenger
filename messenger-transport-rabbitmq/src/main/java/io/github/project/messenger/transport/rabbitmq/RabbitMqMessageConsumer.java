package io.github.project.messenger.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.messenger.dispatch.IncomingMessageDispatcher;
import io.github.project.messenger.exception.MessageDispatchException;
import io.github.project.messenger.transport.TransportMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public final class RabbitMqMessageConsumer {
    private final ObjectMapper objectMapper;
    private final IncomingMessageDispatcher dispatcher;

    public RabbitMqMessageConsumer(ObjectMapper objectMapper, IncomingMessageDispatcher dispatcher) {
        this.objectMapper = objectMapper.findAndRegisterModules();
        this.dispatcher = dispatcher;
    }

    @RabbitListener(
        queues = "#{'${messenger.transports.rabbitmq.consumer.queues:}'.split(',')}",
        autoStartup = "${messenger.transports.rabbitmq.consumer.enabled:false}"
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
