package io.github.corentinguerrero.messenger.transport.kafka;

import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.transport.TransportMessage;

public interface KafkaMessagePublisher {
    void publish(String topic, String key, TransportMessage message, MessageEnvelope envelope, MessageRoute route);
}
