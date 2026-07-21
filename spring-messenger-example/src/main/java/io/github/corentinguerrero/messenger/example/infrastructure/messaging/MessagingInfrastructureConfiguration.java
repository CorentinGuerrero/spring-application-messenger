package io.github.corentinguerrero.messenger.example.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
public class MessagingInfrastructureConfiguration {
    public static final String RABBIT_EXCHANGE = "messenger.example";
    public static final String RABBIT_QUEUE = "messenger.example.mail";
    public static final String RABBIT_ROUTING_KEY = "mail.welcome";
    public static final String KAFKA_USER_INDEX_TOPIC = "messenger.example.user-index";

    @Bean
    DirectExchange messengerExchange() {
        return new DirectExchange(RABBIT_EXCHANGE);
    }

    @Bean
    Queue welcomeEmailQueue() {
        return new Queue(RABBIT_QUEUE, false, false, true);
    }

    @Bean
    Binding welcomeEmailBinding(Queue welcomeEmailQueue, DirectExchange messengerExchange) {
        return BindingBuilder.bind(welcomeEmailQueue).to(messengerExchange).with(RABBIT_ROUTING_KEY);
    }

    @Bean
    MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper.findAndRegisterModules());
    }

    @Bean
    NewTopic userIndexTopic() {
        return TopicBuilder.name(KAFKA_USER_INDEX_TOPIC).partitions(1).replicas(1).build();
    }
}
