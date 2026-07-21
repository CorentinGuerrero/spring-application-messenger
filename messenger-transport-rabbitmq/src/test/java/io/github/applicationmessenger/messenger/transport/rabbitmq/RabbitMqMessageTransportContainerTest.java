package io.github.applicationmessenger.messenger.transport.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RabbitMqMessageTransportContainerTest {
    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-alpine"));

    @Test
    void publishesMessageToRabbitMqQueue() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
        connectionFactory.setUsername(rabbit.getAdminUsername());
        connectionFactory.setPassword(rabbit.getAdminPassword());
        try {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(new ObjectMapper().findAndRegisterModules()));
            RabbitAdmin admin = new RabbitAdmin(connectionFactory);
            admin.declareExchange(new DirectExchange("messenger.test"));
            admin.declareQueue(new Queue("messenger.test.queue", false, false, true));
            admin.declareBinding(BindingBuilder.bind(new Queue("messenger.test.queue")).to(new DirectExchange("messenger.test")).with("mail.welcome"));

            RabbitMqTransportProperties properties = new RabbitMqTransportProperties();
            properties.setExchange("messenger.test");
            properties.getRoutingKeys().put(SendWelcomeEmail.class.getSimpleName(), "mail.welcome");
            RabbitMqMessageTransport transport = new RabbitMqMessageTransport(new DefaultRabbitMqMessagePublisher(rabbitTemplate), properties);

            transport.dispatch(MessageEnvelope.of(new SendWelcomeEmail("42")), new MessageRoute(BusType.COMMAND, SendWelcomeEmail.class, "rabbitmq"), next -> null);

            Message received = rabbitTemplate.receive("messenger.test.queue", 5_000);
            assertThat(received).isNotNull();
            String body = new String(received.getBody(), StandardCharsets.UTF_8);
            assertThat(body).contains("\"messageType\":\"" + SendWelcomeEmail.class.getName() + "\"");
            assertThat(body).contains("\"busType\":\"COMMAND\"");
        } finally {
            connectionFactory.destroy();
        }
    }

    record SendWelcomeEmail(String userId) implements Command<Void> {
    }
}
