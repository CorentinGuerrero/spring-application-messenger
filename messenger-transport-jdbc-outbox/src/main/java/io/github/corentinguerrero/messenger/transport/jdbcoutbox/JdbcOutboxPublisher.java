package io.github.corentinguerrero.messenger.transport.jdbcoutbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.corentinguerrero.messenger.dispatch.IncomingMessageDispatcher;
import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.exception.MessageDispatchException;
import io.github.corentinguerrero.messenger.routing.MessageRoute;
import io.github.corentinguerrero.messenger.transport.MessageTransportRegistry;
import io.github.corentinguerrero.messenger.transport.TransportNames;
import io.github.corentinguerrero.messenger.transport.TransportMessage;
import org.springframework.context.SmartLifecycle;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class JdbcOutboxPublisher implements SmartLifecycle {
    private final OutboxMessageRepository repository;
    private final MessageTransportRegistry transportRegistry;
    private final IncomingMessageDispatcher incomingMessageDispatcher;
    private final ObjectMapper objectMapper;
    private final JdbcOutboxTransportProperties properties;
    private final TransactionTemplate transactionTemplate;
    private ScheduledExecutorService executorService;
    private boolean running;

    public JdbcOutboxPublisher(
        OutboxMessageRepository repository,
        MessageTransportRegistry transportRegistry,
        ObjectMapper objectMapper,
        JdbcOutboxTransportProperties properties,
        TransactionTemplate transactionTemplate
    ) {
        this(repository, transportRegistry, null, objectMapper, properties, transactionTemplate);
    }

    public JdbcOutboxPublisher(
        OutboxMessageRepository repository,
        MessageTransportRegistry transportRegistry,
        IncomingMessageDispatcher incomingMessageDispatcher,
        ObjectMapper objectMapper,
        JdbcOutboxTransportProperties properties,
        TransactionTemplate transactionTemplate
    ) {
        this.repository = repository;
        this.transportRegistry = transportRegistry;
        this.incomingMessageDispatcher = incomingMessageDispatcher;
        this.objectMapper = objectMapper.findAndRegisterModules();
        this.properties = properties;
        this.transactionTemplate = transactionTemplate;
    }

    public int publishOnce() {
        return transactionTemplate.execute(status -> {
            int published = 0;
            for (OutboxMessageRecord record : repository.findPending(properties.getTableName(), properties.getPublisher().getBatchSize())) {
                try {
                    publish(record);
                    repository.markPublished(properties.getTableName(), record.messageId(), Instant.now());
                    published++;
                } catch (Throwable failure) {
                    int attempts = record.attempts() + 1;
                    if (attempts >= properties.getPublisher().getMaxAttempts()) {
                        repository.markFailed(properties.getTableName(), record.messageId(), attempts, failure.getMessage());
                    } else {
                        repository.markRetryable(
                            properties.getTableName(),
                            record.messageId(),
                            attempts,
                            failure.getMessage(),
                            Instant.now().plus(properties.getPublisher().getRetryDelay())
                        );
                    }
                }
            }
            return published;
        });
    }

    @Override
    public void start() {
        if (running || !properties.getPublisher().isEnabled()) {
            return;
        }
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "messenger-jdbc-outbox-publisher");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleWithFixedDelay(
            this::publishOnce,
            0,
            properties.getPublisher().getPollInterval().toMillis(),
            TimeUnit.MILLISECONDS
        );
        running = true;
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    private void publish(OutboxMessageRecord record) {
        try {
            TransportMessage message = objectMapper.readValue(record.payload(), TransportMessage.class);
            String targetTransport = targetTransport(message);
            Object typedPayload = objectMapper.convertValue(message.payload(), Class.forName(message.messageType()));
            if (Objects.equals(targetTransport, TransportNames.SYNC)) {
                if (incomingMessageDispatcher == null) {
                    throw new MessageDispatchException("No incoming dispatcher available for sync outbox publishing", null);
                }
                incomingMessageDispatcher.dispatch(message.busType(), typedPayload, message.metadata());
                return;
            }
            MessageEnvelope envelope = new MessageEnvelope(typedPayload, message.metadata());
            MessageRoute route = new MessageRoute(message.busType(), typedPayload.getClass(), targetTransport);
            transportRegistry.dispatch(envelope, route, next -> null);
        } catch (JsonProcessingException | ClassNotFoundException exception) {
            throw new MessageDispatchException("Could not publish outbox message " + record.messageId(), exception);
        }
    }

    private String targetTransport(TransportMessage message) {
        String configured = properties.getPublisher().getRoutes().get(message.messageType());
        if (configured == null) {
            configured = properties.getPublisher().getRoutes().get(simpleName(message.messageType()));
        }
        if (configured == null) {
            configured = properties.getPublisher().getRoutes().get("*");
        }
        if (configured == null) {
            configured = properties.getPublisher().getTargetTransport();
        }
        if (configured == null || configured.isBlank()) {
            throw new MessageDispatchException("No outbox target transport configured for " + message.messageType(), null);
        }
        String target = configured.toLowerCase(Locale.ROOT).trim();
        if (Objects.equals(target, JdbcOutboxMessageTransport.TRANSPORT_NAME) || Objects.equals(target, JdbcOutboxMessageTransport.PG_ALIAS)) {
            throw new MessageDispatchException("Outbox publisher target transport cannot be " + target, null);
        }
        return target;
    }

    private static String simpleName(String className) {
        int separator = className.lastIndexOf('.');
        if (separator < 0) {
            return className;
        }
        return className.substring(separator + 1);
    }
}
