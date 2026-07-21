package io.github.corentinguerrero.messenger.transport.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.dispatch.IncomingMessageDispatcher;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaTransportProperties.class)
@ConditionalOnProperty(prefix = "messenger.transports.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableKafka
public class KafkaTransportAutoConfiguration {
    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean
    KafkaMessagePublisher kafkaMessagePublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DefaultKafkaMessagePublisher(kafkaTemplate);
    }

    @Bean
    @ConditionalOnBean(KafkaMessagePublisher.class)
    @ConditionalOnMissingBean(name = "kafkaMessageTransport")
    MessageTransport kafkaMessageTransport(KafkaMessagePublisher publisher, KafkaTransportProperties properties) {
        return new KafkaMessageTransport(publisher, properties);
    }

    @Bean
    @ConditionalOnBean({IncomingMessageDispatcher.class, ObjectMapper.class})
    @ConditionalOnMissingBean
    KafkaTransportMessageConsumer kafkaTransportMessageConsumer(ObjectMapper objectMapper, IncomingMessageDispatcher dispatcher) {
        return new KafkaTransportMessageConsumer(objectMapper, dispatcher);
    }
}
