package io.github.applicationmessenger.messenger.envelope;

import io.github.applicationmessenger.messenger.api.PublicApi;

import java.util.Objects;

@PublicApi
public final class MessageEnvelope {
    private final Object payload;
    private final MessageMetadata metadata;

    public MessageEnvelope(Object payload, MessageMetadata metadata) {
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
    }

    public static MessageEnvelope of(Object payload) {
        return new MessageEnvelope(payload, MessageMetadata.create());
    }

    public Object payload() {
        return payload;
    }

    public Object getPayload() {
        return payload;
    }

    public MessageMetadata metadata() {
        return metadata;
    }

    public MessageMetadata getMetadata() {
        return metadata;
    }
}
