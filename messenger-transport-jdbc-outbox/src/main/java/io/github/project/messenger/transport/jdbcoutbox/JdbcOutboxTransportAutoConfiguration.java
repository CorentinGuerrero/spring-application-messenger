package io.github.project.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.project.messenger.transport.MessageTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(
    afterName = {
        "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration",
        "org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration"
    },
    beforeName = "io.github.project.messenger.autoconfigure.MessengerAutoConfiguration"
)
@ConditionalOnClass(JdbcTemplate.class)
@EnableConfigurationProperties(JdbcOutboxTransportProperties.class)
@ConditionalOnProperty(prefix = "messenger.transports.jdbc-outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JdbcOutboxTransportAutoConfiguration {
    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    OutboxMessageRepository outboxMessageRepository(JdbcTemplate jdbcTemplate) {
        return new DefaultOutboxMessageRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnBean(OutboxMessageRepository.class)
    @ConditionalOnMissingBean
    JdbcOutboxSchemaInitializer jdbcOutboxSchemaInitializer(OutboxMessageRepository repository, JdbcOutboxTransportProperties properties) {
        return new JdbcOutboxSchemaInitializer(repository, properties);
    }

    @Bean
    @ConditionalOnBean({OutboxMessageRepository.class, ObjectMapper.class})
    @ConditionalOnMissingBean(name = "jdbcOutboxMessageTransport")
    MessageTransport jdbcOutboxMessageTransport(OutboxMessageRepository repository, ObjectMapper objectMapper, JdbcOutboxTransportProperties properties) {
        return new JdbcOutboxMessageTransport(JdbcOutboxMessageTransport.TRANSPORT_NAME, repository, objectMapper, properties);
    }

    @Bean
    @ConditionalOnBean({OutboxMessageRepository.class, ObjectMapper.class})
    @ConditionalOnMissingBean(name = "pgMessageTransport")
    MessageTransport pgMessageTransport(OutboxMessageRepository repository, ObjectMapper objectMapper, JdbcOutboxTransportProperties properties) {
        return new JdbcOutboxMessageTransport(JdbcOutboxMessageTransport.PG_ALIAS, repository, objectMapper, properties);
    }
}
