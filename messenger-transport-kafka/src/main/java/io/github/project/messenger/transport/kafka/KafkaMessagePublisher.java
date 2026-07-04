package io.github.project.messenger.transport.kafka;

import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.routing.MessageRoute;
import io.github.project.messenger.transport.TransportMessage;

public interface KafkaMessagePublisher {
    void publish(String topic, String key, TransportMessage message, MessageEnvelope envelope, MessageRoute route);
}
