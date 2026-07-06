package io.github.corentinguerrero.messenger.transport.rabbitmq;

import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMqTransportAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(RabbitMqTransportAutoConfiguration.class));

    @Test
    void createsRabbitMqTransportWhenPublisherExists() {
        contextRunner
            .withUserConfiguration(PublisherConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(MessageTransport.class);
                assertThat(context.getBean(MessageTransport.class).name()).isEqualTo("rabbitmq");
            });
    }

    @Test
    void doesNotCreateTransportWhenDisabled() {
        contextRunner
            .withUserConfiguration(PublisherConfiguration.class)
            .withPropertyValues("messenger.transports.rabbitmq.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(MessageTransport.class));
    }

    static class PublisherConfiguration {
        @Bean
        RabbitMqMessagePublisher rabbitMqMessagePublisher() {
            return (exchange, routingKey, message, envelope, route) -> {
            };
        }
    }
}
