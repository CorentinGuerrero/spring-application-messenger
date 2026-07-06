package io.github.corentinguerrero.messenger.autoconfigure;

import io.github.corentinguerrero.messenger.CommandBus;
import io.github.corentinguerrero.messenger.EventBus;
import io.github.corentinguerrero.messenger.QueryBus;
import io.github.corentinguerrero.messenger.dispatch.DefaultCommandBus;
import io.github.corentinguerrero.messenger.dispatch.DefaultEventBus;
import io.github.corentinguerrero.messenger.dispatch.DefaultQueryBus;
import io.github.corentinguerrero.messenger.dispatch.IncomingMessageDispatcher;
import io.github.corentinguerrero.messenger.handler.HandlerRegistry;
import io.github.corentinguerrero.messenger.middleware.MessageMiddleware;
import io.github.corentinguerrero.messenger.routing.DefaultMessageRouter;
import io.github.corentinguerrero.messenger.routing.MessageRouter;
import io.github.corentinguerrero.messenger.spring.HandlerMethodScanner;
import io.github.corentinguerrero.messenger.transport.InProcessMessageTransport;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import io.github.corentinguerrero.messenger.transport.MessageTransportRegistry;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration(after = ValidationAutoConfiguration.class)
@EnableConfigurationProperties(MessengerProperties.class)
@ConditionalOnProperty(prefix = "messenger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessengerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    HandlerRegistry handlerRegistry(ApplicationContext applicationContext) {
        return new HandlerMethodScanner(applicationContext).scan();
    }

    @Bean
    @ConditionalOnMissingBean
    MessageRouter messageRouter(MessengerProperties properties) {
        return new DefaultMessageRouter(
            properties.getDefaultTransport(),
            properties.getRouting().getCommands(),
            properties.getRouting().getEvents()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "inProcessMessageTransport")
    InProcessMessageTransport inProcessMessageTransport() {
        return new InProcessMessageTransport();
    }

    @Bean
    @ConditionalOnMissingBean
    MessageTransportRegistry messageTransportRegistry(List<MessageTransport> transports) {
        return new MessageTransportRegistry(transports);
    }

    @Bean
    @ConditionalOnMissingBean
    CommandBus commandBus(HandlerRegistry handlerRegistry, List<MessageMiddleware> middlewares, MessageRouter router, MessageTransportRegistry transportRegistry) {
        return new DefaultCommandBus(handlerRegistry, middlewares, router, transportRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    QueryBus queryBus(HandlerRegistry handlerRegistry, List<MessageMiddleware> middlewares) {
        return new DefaultQueryBus(handlerRegistry, middlewares);
    }

    @Bean
    @ConditionalOnMissingBean
    EventBus eventBus(HandlerRegistry handlerRegistry, List<MessageMiddleware> middlewares, MessengerProperties properties, MessageRouter router, MessageTransportRegistry transportRegistry) {
        return new DefaultEventBus(handlerRegistry, middlewares, properties.getEvents().getErrorStrategy(), router, transportRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    IncomingMessageDispatcher incomingMessageDispatcher(HandlerRegistry handlerRegistry, List<MessageMiddleware> middlewares, MessengerProperties properties) {
        return new IncomingMessageDispatcher(handlerRegistry, middlewares, properties.getEvents().getErrorStrategy());
    }

    @Bean
    @ConditionalOnClass(Validator.class)
    @ConditionalOnBean(Validator.class)
    @ConditionalOnMissingBean(ValidationMessageMiddleware.class)
    @ConditionalOnProperty(prefix = "messenger.validation", name = "enabled", havingValue = "true")
    ValidationMessageMiddleware validationMessageMiddleware(Validator validator) {
        return new ValidationMessageMiddleware(validator);
    }
}
