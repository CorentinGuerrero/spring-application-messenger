package io.github.corentinguerrero.messenger.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.dispatch.IncomingMessageDispatcher;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = RabbitAutoConfiguration.class)
@ConditionalOnClass(RabbitTemplate.class)
@EnableConfigurationProperties(RabbitMqTransportProperties.class)
@ConditionalOnProperty(prefix = "messenger.transports.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableRabbit
public class RabbitMqTransportAutoConfiguration {
    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    @ConditionalOnMissingBean
    RabbitMqMessagePublisher rabbitMqMessagePublisher(RabbitTemplate rabbitTemplate) {
        return new DefaultRabbitMqMessagePublisher(rabbitTemplate);
    }

    @Bean
    @ConditionalOnBean(RabbitMqMessagePublisher.class)
    @ConditionalOnMissingBean(name = "rabbitMqMessageTransport")
    MessageTransport rabbitMqMessageTransport(RabbitMqMessagePublisher publisher, RabbitMqTransportProperties properties) {
        return new RabbitMqMessageTransport(publisher, properties);
    }

    @Bean
    @ConditionalOnBean({IncomingMessageDispatcher.class, ObjectMapper.class})
    @ConditionalOnMissingBean
    RabbitMqMessageConsumer rabbitMqMessageConsumer(ObjectMapper objectMapper, IncomingMessageDispatcher dispatcher) {
        return new RabbitMqMessageConsumer(objectMapper, dispatcher);
    }
}
