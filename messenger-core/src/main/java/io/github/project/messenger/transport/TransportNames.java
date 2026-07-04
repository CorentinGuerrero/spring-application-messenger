package io.github.project.messenger.transport;

import io.github.project.messenger.api.PublicApi;

@PublicApi
public final class TransportNames {
    public static final String SYNC = "sync";
    public static final String RABBITMQ = "rabbitmq";
    public static final String KAFKA = "kafka";
    public static final String REDIS = "redis";
    public static final String JDBC_OUTBOX = "pg-outbox";
    public static final String PG_OUTBOX_ALIAS = "pg";

    private TransportNames() {
    }
}
