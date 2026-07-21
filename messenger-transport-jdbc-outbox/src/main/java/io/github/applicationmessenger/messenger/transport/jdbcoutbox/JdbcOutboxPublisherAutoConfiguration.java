package io.github.applicationmessenger.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.applicationmessenger.messenger.dispatch.IncomingMessageDispatcher;
import io.github.applicationmessenger.messenger.transport.MessageTransportRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration(afterName = "io.github.applicationmessenger.messenger.autoconfigure.MessengerAutoConfiguration")
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnProperty(prefix = "messenger.transports.jdbc-outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JdbcOutboxPublisherAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    JdbcOutboxPublisher jdbcOutboxPublisher(
        OutboxMessageRepository repository,
        MessageTransportRegistry transportRegistry,
        ObjectProvider<IncomingMessageDispatcher> incomingMessageDispatcher,
        ObjectMapper objectMapper,
        JdbcOutboxTransportProperties properties,
        PlatformTransactionManager transactionManager
    ) {
        return new JdbcOutboxPublisher(
            repository,
            transportRegistry,
            incomingMessageDispatcher.getIfAvailable(),
            objectMapper,
            properties,
            new TransactionTemplate(transactionManager)
        );
    }
}
