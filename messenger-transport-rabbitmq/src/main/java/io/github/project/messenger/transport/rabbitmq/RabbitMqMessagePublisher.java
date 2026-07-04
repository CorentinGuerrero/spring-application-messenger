package io.github.project.messenger.transport.rabbitmq;

import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.transport.TransportMessage;

public interface RabbitMqMessagePublisher {
    void publish(String exchange, String routingKey, TransportMessage message, MessageEnvelope envelope, MessageRoute route);
}
