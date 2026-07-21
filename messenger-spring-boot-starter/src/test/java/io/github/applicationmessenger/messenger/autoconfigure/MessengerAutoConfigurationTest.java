package io.github.applicationmessenger.messenger.autoconfigure;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.CommandBus;
import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.envelope.MessageEnvelope;
import io.github.applicationmessenger.messenger.handler.MessageHandler;
import io.github.applicationmessenger.messenger.routing.MessageRoute;
import io.github.applicationmessenger.messenger.EventBus;
import io.github.applicationmessenger.messenger.QueryBus;
import io.github.applicationmessenger.messenger.spring.CommandHandler;
import io.github.applicationmessenger.messenger.transport.MessageTransport;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessengerAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MessengerAutoConfiguration.class, ValidationAutoConfiguration.class));

    @Test
    void createsDefaultBusesAndScansHandlers() {
        contextRunner
            .withUserConfiguration(CreateGreetingHandler.class)
            .run(context -> {
                assertThat(context).hasSingleBean(CommandBus.class);
                assertThat(context).hasSingleBean(QueryBus.class);
                assertThat(context).hasSingleBean(EventBus.class);

                CommandBus commandBus = context.getBean(CommandBus.class);
                assertThat(commandBus.dispatch(new CreateGreeting("Ada"))).isEqualTo("created Ada");
            });
    }

    @Test
    void validationMiddlewareCanBeEnabled() {
        contextRunner
            .withPropertyValues("messenger.validation.enabled=true")
            .withUserConfiguration(CreateGreetingHandler.class)
            .run(context -> {
                CommandBus commandBus = context.getBean(CommandBus.class);

                assertThatThrownBy(() -> commandBus.dispatch(new CreateGreeting("")))
                    .isInstanceOf(ConstraintViolationException.class);
            });
    }

    @Test
    void routesMessagesToConfiguredTransport() {
        contextRunner
            .withPropertyValues("messenger.routing.commands.CreateGreeting=capture")
            .withUserConfiguration(CreateGreetingHandler.class, CaptureTransportConfiguration.class)
            .run(context -> {
                CommandBus commandBus = context.getBean(CommandBus.class);
                CaptureTransport transport = context.getBean(CaptureTransport.class);

                assertThat(commandBus.dispatch(new CreateGreeting("Ada"))).isEqualTo("captured");
                assertThat(transport.route.busType()).isEqualTo(BusType.COMMAND);
                assertThat(transport.route.messageType()).isEqualTo(CreateGreeting.class);
                assertThat(transport.route.transportName()).isEqualTo("capture");
            });
    }

    record CreateGreeting(@NotBlank String name) implements Command<String> {
    }

    @CommandHandler
    static class CreateGreetingHandler {
        String handle(CreateGreeting command) {
            return "created " + command.name();
        }
    }

    static class CaptureTransportConfiguration {
        @Bean
        CaptureTransport captureTransport() {
            return new CaptureTransport();
        }
    }

    static class CaptureTransport implements MessageTransport {
        private MessageRoute route;

        @Override
        public String name() {
            return "capture";
        }

        @Override
        public Object dispatch(MessageEnvelope envelope, MessageRoute route, MessageHandler next) {
            this.route = route;
            return "captured";
        }
    }
}
