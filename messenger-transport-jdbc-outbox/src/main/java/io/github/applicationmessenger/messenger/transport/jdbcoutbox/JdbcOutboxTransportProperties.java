package io.github.applicationmessenger.messenger.transport.jdbcoutbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "messenger.transports.jdbc-outbox")
public class JdbcOutboxTransportProperties {
    private boolean enabled = true;
    private boolean initializeSchema = true;
    private String tableName = "messenger_outbox";
    private String initialStatus = "PENDING";
    private final Publisher publisher = new Publisher();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isInitializeSchema() {
        return initializeSchema;
    }

    public void setInitializeSchema(boolean initializeSchema) {
        this.initializeSchema = initializeSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public static final class Publisher {
        private boolean enabled;
        private int batchSize = 50;
        private int maxAttempts = 3;
        private Duration pollInterval = Duration.ofSeconds(5);
        private Duration retryDelay = Duration.ofSeconds(30);
        private String targetTransport;
        private Map<String, String> routes = new LinkedHashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getPollInterval() {
            return pollInterval;
        }

        public void setPollInterval(Duration pollInterval) {
            this.pollInterval = pollInterval;
        }

        public Duration getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
        }

        public String getTargetTransport() {
            return targetTransport;
        }

        public void setTargetTransport(String targetTransport) {
            this.targetTransport = targetTransport;
        }

        public Map<String, String> getRoutes() {
            return routes;
        }

        public void setRoutes(Map<String, String> routes) {
            this.routes = routes;
        }
    }
}
