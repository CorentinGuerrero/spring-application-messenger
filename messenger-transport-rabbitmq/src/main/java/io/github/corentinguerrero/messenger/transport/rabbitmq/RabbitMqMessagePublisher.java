package io.github.corentinguerrero.messenger.transport.rabbitmq;

import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.transport.TransportMessage;

public interface RabbitMqMessagePublisher {
    void publish(String exchange, String routingKey, TransportMessage message, MessageEnvelope envelope, MessageRoute route);
}
