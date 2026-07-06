package io.github.corentinguerrero.messenger.transport.redis;

import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class RedisTransportAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(RedisTransportAutoConfiguration.class));

    @Test
    void createsRedisTransportWhenPublisherExists() {
        contextRunner
            .withUserConfiguration(PublisherConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(MessageTransport.class);
                assertThat(context.getBean(MessageTransport.class).name()).isEqualTo("redis");
            });
    }

    @Test
    void doesNotCreateTransportWhenDisabled() {
        contextRunner
            .withUserConfiguration(PublisherConfiguration.class)
            .withPropertyValues("messenger.transports.redis.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(MessageTransport.class));
    }

    static class PublisherConfiguration {
        @Bean
        RedisStreamMessagePublisher redisStreamMessagePublisher() {
            return (stream, body) -> {
            };
        }
    }
}
