package io.github.corentinguerrero.messenger.transport.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.dispatch.IncomingMessageDispatcher;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(RedisTransportProperties.class)
@ConditionalOnProperty(prefix = "messenger.transports.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisTransportAutoConfiguration {
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    RedisStreamMessagePublisher redisStreamMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        return new DefaultRedisStreamMessagePublisher(redisTemplate);
    }

    @Bean
    @ConditionalOnBean(RedisStreamMessagePublisher.class)
    @ConditionalOnMissingBean(name = "redisMessageTransport")
    MessageTransport redisMessageTransport(RedisStreamMessagePublisher publisher, RedisTransportProperties properties) {
        return new RedisMessageTransport(publisher, properties);
    }

    @Bean
    @ConditionalOnBean({IncomingMessageDispatcher.class, ObjectMapper.class})
    @ConditionalOnMissingBean
    RedisTransportMessageConsumer redisTransportMessageConsumer(ObjectMapper objectMapper, IncomingMessageDispatcher dispatcher) {
        return new RedisTransportMessageConsumer(objectMapper, dispatcher);
    }

    @Bean
    @ConditionalOnBean({RedisConnectionFactory.class, RedisTemplate.class, RedisTransportMessageConsumer.class})
    @ConditionalOnMissingBean
    ManagedRedisStreamMessageListener managedRedisStreamMessageListener(
        RedisConnectionFactory connectionFactory,
        RedisTemplate<String, Object> redisTemplate,
        RedisTransportMessageConsumer consumer,
        RedisTransportProperties properties,
        ObjectMapper objectMapper
    ) {
        return new ManagedRedisStreamMessageListener(connectionFactory, redisTemplate, consumer, properties, objectMapper);
    }
}
