package io.github.project.messenger.transport.jdbcoutbox;

import org.springframework.beans.factory.InitializingBean;

public final class JdbcOutboxSchemaInitializer implements InitializingBean {
    private final OutboxMessageRepository repository;
    private final JdbcOutboxTransportProperties properties;

    public JdbcOutboxSchemaInitializer(OutboxMessageRepository repository, JdbcOutboxTransportProperties properties) {
        this.repository = repository;
        this.properties = properties;
        validateTableName(properties.getTableName());
    }

    @Override
    public void afterPropertiesSet() {
        if (!properties.isInitializeSchema()) {
            return;
        }

        repository.execute("""
            create table if not exists %s (
              message_id uuid primary key,
              correlation_id uuid not null,
              causation_id uuid null,
              bus_type varchar(32) not null,
              message_type varchar(512) not null,
              transport_name varchar(64) not null,
              payload text not null,
              status varchar(32) not null,
              created_at timestamp not null,
              attempts integer not null default 0,
              last_error text null,
              next_attempt_at timestamp null,
              published_at timestamp null
            )
            """.formatted(properties.getTableName()));
        repository.execute("alter table %s add column if not exists attempts integer not null default 0".formatted(properties.getTableName()));
        repository.execute("alter table %s add column if not exists last_error text null".formatted(properties.getTableName()));
        repository.execute("alter table %s add column if not exists next_attempt_at timestamp null".formatted(properties.getTableName()));
        repository.execute("alter table %s add column if not exists published_at timestamp null".formatted(properties.getTableName()));
        repository.execute("""
            create index if not exists %s
              on %s (status, next_attempt_at, created_at)
            """.formatted(indexName(properties.getTableName()), properties.getTableName()));
    }

    private static String indexName(String tableName) {
        return tableName.replace('.', '_') + "_idx";
    }

    private static void validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("Invalid outbox table name: " + tableName);
        }
    }
}
