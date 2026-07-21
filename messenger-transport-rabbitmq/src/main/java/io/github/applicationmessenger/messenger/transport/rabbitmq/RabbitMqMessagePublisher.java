package io.github.applicationmessenger.messenger.transport.rabbitmq;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.TransportMessage;

public interface RabbitMqMessagePublisher {
    void publish(String exchange, String routingKey, TransportMessage message, MessageEnvelope envelope, MessageRoute route);
}
