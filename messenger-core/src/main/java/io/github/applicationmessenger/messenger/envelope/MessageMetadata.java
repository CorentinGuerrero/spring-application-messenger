package io.github.applicationmessenger.messenger.envelope;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.applicationmessenger.messenger.api.PublicApi;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@PublicApi
public final class MessageMetadata {
    private final UUID messageId;
    private final UUID correlationId;
    private final UUID causationId;
    private final Instant createdAt;
    private final Map<String, Object> headers;

    @JsonCreator
    public MessageMetadata(
        @JsonProperty("messageId") UUID messageId,
        @JsonProperty("correlationId") UUID correlationId,
        @JsonProperty("causationId") UUID causationId,
        @JsonProperty("createdAt") Instant createdAt,
        @JsonProperty("headers") Map<String, Object> headers
    ) {
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.createdAt = createdAt;
        this.headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static MessageMetadata create() {
        return create(Clock.systemUTC());
    }

    public static MessageMetadata create(Clock clock) {
        UUID messageId = UUID.randomUUID();
        return new MessageMetadata(messageId, messageId, null, Instant.now(clock), Map.of());
    }

    public MessageMetadata withHeader(String name, Object value) {
        Map<String, Object> nextHeaders = new LinkedHashMap<>(headers);
        nextHeaders.put(name, value);
        return new MessageMetadata(messageId, correlationId, causationId, createdAt, nextHeaders);
    }

    public UUID messageId() {
        return messageId;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID correlationId() {
        return correlationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID causationId() {
        return causationId;
    }

    public UUID getCausationId() {
        return causationId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> headers() {
        return headers;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
}
