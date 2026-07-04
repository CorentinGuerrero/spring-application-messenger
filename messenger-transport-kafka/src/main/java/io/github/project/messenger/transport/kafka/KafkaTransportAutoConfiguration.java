package io.github.project.messenger.transport.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.messenger.dispatch.IncomingMessageDispatcher;
import io.github.project.messenger.transport.MessageTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaTransportProperties.class)
@ConditionalOnProperty(prefix = "messenger.transports.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
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
