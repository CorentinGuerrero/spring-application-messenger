package io.github.applicationmessenger.messenger.transport.kafka;

import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.transport.TransportMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;

final class DefaultKafkaMessagePublisher implements KafkaMessagePublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    DefaultKafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String key, TransportMessage message, MessageEnvelope envelope, MessageRoute route) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, message);
        addHeaders(record.headers(), envelope, route);
        kafkaTemplate.send(record);
    }

    private static void addHeaders(Headers headers, MessageEnvelope envelope, MessageRoute route) {
        headers.add("messenger_message_id", envelope.metadata().messageId().toString().getBytes(StandardCharsets.UTF_8));
        headers.add("messenger_correlation_id", envelope.metadata().correlationId().toString().getBytes(StandardCharsets.UTF_8));
        if (envelope.metadata().causationId() != null) {
            headers.add("messenger_causation_id", envelope.metadata().causationId().toString().getBytes(StandardCharsets.UTF_8));
        }
        headers.add("messenger_bus_type", route.busType().name().getBytes(StandardCharsets.UTF_8));
        headers.add("messenger_message_type", route.messageType().getName().getBytes(StandardCharsets.UTF_8));
    }
}
