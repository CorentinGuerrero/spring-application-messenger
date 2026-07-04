package io.github.project.messenger.transport.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.messenger.transport.TransportMessage;
import org.springframework.context.SmartLifecycle;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;

public final class ManagedRedisStreamMessageListener implements SmartLifecycle {
    private final RedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransportMessageConsumer consumer;
    private final RedisTransportProperties properties;
    private final ObjectMapper objectMapper;
    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> container;
    private boolean running;

    public ManagedRedisStreamMessageListener(
        RedisConnectionFactory connectionFactory,
        RedisTemplate<String, Object> redisTemplate,
        RedisTransportMessageConsumer consumer,
        RedisTransportProperties properties,
        ObjectMapper objectMapper
    ) {
        this.connectionFactory = connectionFactory;
        this.redisTemplate = redisTemplate;
        this.consumer = consumer;
        this.properties = properties;
        this.objectMapper = objectMapper.findAndRegisterModules();
    }

    @Override
    public void start() {
        if (running || !properties.getConsumer().isEnabled()) {
            return;
        }
        List<String> streams = streams();
        if (streams.isEmpty()) {
            return;
        }

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, Object, Object>> options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(properties.getConsumer().getPollTimeout())
                .serializer(RedisSerializer.string())
                .hashKeySerializer(StringRedisSerializer.UTF_8)
                .hashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper))
                .build();

        container = StreamMessageListenerContainer.create(connectionFactory, options);
        for (String stream : streams) {
            ensureGroup(stream);
            container.receive(
                Consumer.from(properties.getConsumer().getGroup(), properties.getConsumer().getConsumerName()),
                StreamOffset.create(stream, ReadOffset.lastConsumed()),
                record -> consume(stream, record)
            );
        }
        container.start();
        running = true;
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void consume(String stream, MapRecord<String, Object, Object> record) {
        Object rawMessage = record.getValue().get("message");
        TransportMessage message = objectMapper.convertValue(rawMessage, TransportMessage.class);
        consumer.consume(message);
        redisTemplate.opsForStream().acknowledge(stream, properties.getConsumer().getGroup(), record.getId());
    }

    private List<String> streams() {
        if (!properties.getConsumer().getStreams().isEmpty()) {
            return properties.getConsumer().getStreams();
        }
        return new ArrayList<>(properties.getStreams().values());
    }

    private void ensureGroup(String stream) {
        try {
            if (redisTemplate.hasKey(stream) == Boolean.FALSE) {
                redisTemplate.opsForStream().add(MapRecord.create(stream, java.util.Map.of("__messenger_init", "true")));
                redisTemplate.opsForStream().trim(stream, 0);
            }
            redisTemplate.opsForStream().createGroup(stream, ReadOffset.from("0-0"), properties.getConsumer().getGroup());
        } catch (DataAccessException exception) {
            if (!String.valueOf(exception.getMessage()).contains("BUSYGROUP")) {
                throw exception;
            }
        }
    }
}
