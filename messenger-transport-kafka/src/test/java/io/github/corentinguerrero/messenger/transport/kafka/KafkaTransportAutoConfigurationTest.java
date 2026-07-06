package io.github.corentinguerrero.messenger.transport.kafka;

import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTransportAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KafkaTransportAutoConfiguration.class));

    @Test
    void createsKafkaTransportWhenPublisherExists() {
        contextRunner
            .withUserConfiguration(PublisherConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(MessageTransport.class);
                assertThat(context.getBean(MessageTransport.class).name()).isEqualTo("kafka");
            });
    }

    @Test
    void doesNotCreateTransportWhenDisabled() {
        contextRunner
            .withUserConfiguration(PublisherConfiguration.class)
            .withPropertyValues("messenger.transports.kafka.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(MessageTransport.class));
    }

    static class PublisherConfiguration {
        @Bean
        KafkaMessagePublisher kafkaMessagePublisher() {
            return (topic, key, message, envelope, route) -> {
            };
        }
    }
}
