package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.transport.MessageTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcOutboxTransportAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JdbcOutboxTransportAutoConfiguration.class));

    @Test
    void createsPgOutboxAndPgAliasWhenRepositoryAndObjectMapperExist() {
        contextRunner
            .withUserConfiguration(OutboxConfiguration.class)
            .run(context -> {
                assertThat(context).hasBean("jdbcOutboxMessageTransport");
                assertThat(context).hasBean("pgMessageTransport");
                assertThat(context.getBeansOfType(MessageTransport.class)).hasSize(2);
                assertThat(context.getBean("jdbcOutboxMessageTransport", MessageTransport.class).name()).isEqualTo("pg-outbox");
                assertThat(context.getBean("pgMessageTransport", MessageTransport.class).name()).isEqualTo("pg");
            });
    }

    @Test
    void doesNotCreateTransportsWhenDisabled() {
        contextRunner
            .withUserConfiguration(OutboxConfiguration.class)
            .withPropertyValues("messenger.transports.jdbc-outbox.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(MessageTransport.class));
    }

    static class OutboxConfiguration {
        @Bean
        OutboxMessageRepository outboxMessageRepository() {
            return new OutboxMessageRepository() {
                @Override
                public int insert(String sql, Object... args) {
                    return 1;
                }

                @Override
                public void execute(String sql) {
                }
            };
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }
}
