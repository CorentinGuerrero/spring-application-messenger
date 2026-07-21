package io.github.applicationmessenger.messenger.transport.kafka;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.TransportMessage;

public interface KafkaMessagePublisher {
    void publish(String topic, String key, TransportMessage message, MessageEnvelope envelope, MessageRoute route);
}
