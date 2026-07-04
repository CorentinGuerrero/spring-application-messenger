package io.github.project.messenger.transport.redis;

import io.github.project.messenger.Command;
import io.github.project.messenger.dispatch.BusType;
import io.github.project.messenger.dispatch.EventErrorStrategy;
import io.github.project.messenger.dispatch.IncomingMessageDispatcher;
import io.github.project.messenger.envelope.MessageEnvelope;
import io.github.project.messenger.handler.InMemoryHandlerRegistry;
import io.github.project.messenger.routing.MessageRoute;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RedisMessageTransportContainerTest {
    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    @Test
    void appendsMessageToRedisStream() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        try {
            RedisTemplate<String, Object> redisTemplate = redisTemplate(connectionFactory);
            RedisTransportProperties properties = new RedisTransportProperties();
            properties.getStreams().put(RebuildSearchIndex.class.getSimpleName(), "messenger:jobs");
            RedisMessageTransport transport = new RedisMessageTransport(new DefaultRedisStreamMessagePublisher(redisTemplate), properties);

            transport.dispatch(MessageEnvelope.of(new RebuildSearchIndex("users")), new MessageRoute(BusType.COMMAND, RebuildSearchIndex.class, "redis"), next -> null);

            List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                .read(StreamOffset.fromStart("messenger:jobs"));
            assertThat(records).hasSize(1);
            assertThat(records.get(0).getValue()).containsEntry("busType", "COMMAND");
            assertThat(records.get(0).getValue()).containsEntry("messageType", RebuildSearchIndex.class.getName());
        } finally {
            connectionFactory.destroy();
        }
    }

    @Test
    void managedListenerConsumesMessageAndAcknowledgesIt() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        ManagedRedisStreamMessageListener listener = null;
        try {
            RedisTemplate<String, Object> redisTemplate = redisTemplate(connectionFactory);
            RedisTransportProperties properties = new RedisTransportProperties();
            properties.getStreams().put(RebuildSearchIndex.class.getSimpleName(), "messenger:managed");
            properties.getConsumer().setEnabled(true);
            properties.getConsumer().setStreams(List.of("messenger:managed"));
            properties.getConsumer().setGroup("messenger-test");
            properties.getConsumer().setConsumerName("worker-1");
            AtomicReference<String> handledIndex = new AtomicReference<>();
            InMemoryHandlerRegistry registry = InMemoryHandlerRegistry.builder()
                .commandHandler(RebuildSearchIndex.class, envelope -> {
                    handledIndex.set(((RebuildSearchIndex) envelope.payload()).name());
                    return null;
                })
                .build();
            IncomingMessageDispatcher dispatcher = new IncomingMessageDispatcher(registry, List.of(), EventErrorStrategy.FAIL_FAST);
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            RedisTransportMessageConsumer consumer = new RedisTransportMessageConsumer(objectMapper, dispatcher);
            listener = new ManagedRedisStreamMessageListener(connectionFactory, redisTemplate, consumer, properties, objectMapper);
            listener.start();
            RedisMessageTransport transport = new RedisMessageTransport(new DefaultRedisStreamMessagePublisher(redisTemplate), properties);

            transport.dispatch(MessageEnvelope.of(new RebuildSearchIndex("products")), new MessageRoute(BusType.COMMAND, RebuildSearchIndex.class, "redis"), next -> null);

            await().untilAsserted(() -> assertThat(handledIndex).hasValue("products"));
            assertThat(redisTemplate.opsForStream().pending("messenger:managed", "messenger-test").getTotalPendingMessages()).isZero();
        } finally {
            if (listener != null) {
                listener.stop();
            }
            connectionFactory.destroy();
        }
    }

    private static RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setHashKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper().findAndRegisterModules()));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    record RebuildSearchIndex(String name) implements Command<Void> {
    }
}
