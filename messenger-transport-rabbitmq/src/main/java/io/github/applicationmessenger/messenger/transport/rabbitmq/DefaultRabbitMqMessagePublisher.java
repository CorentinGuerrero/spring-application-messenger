package io.github.applicationmessenger.messenger.transport.rabbitmq;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.TransportMessage;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

final class DefaultRabbitMqMessagePublisher implements RabbitMqMessagePublisher {
    private final RabbitTemplate rabbitTemplate;

    DefaultRabbitMqMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String exchange, String routingKey, TransportMessage message, MessageEnvelope envelope, MessageRoute route) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, metadataHeaders(envelope, route));
    }

    private MessagePostProcessor metadataHeaders(MessageEnvelope envelope, MessageRoute route) {
        return message -> {
            message.getMessageProperties().setHeader("messenger_message_id", envelope.metadata().messageId().toString());
            message.getMessageProperties().setHeader("messenger_correlation_id", envelope.metadata().correlationId().toString());
            message.getMessageProperties().setHeader("messenger_causation_id", envelope.metadata().causationId() == null ? null : envelope.metadata().causationId().toString());
            message.getMessageProperties().setHeader("messenger_bus_type", route.busType().name());
            message.getMessageProperties().setHeader("messenger_message_type", route.messageType().getName());
            return message;
        };
    }
}
