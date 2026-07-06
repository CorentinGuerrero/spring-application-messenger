package io.github.corentinguerrero.messenger.transport.rabbitmq;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "messenger.transports.rabbitmq")
public class RabbitMqTransportProperties {
    private boolean enabled = true;
    private String exchange = "messenger";
    private String routingKeyPrefix = "messenger.";
    private Map<String, String> routingKeys = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKeyPrefix() {
        return routingKeyPrefix;
    }

    public void setRoutingKeyPrefix(String routingKeyPrefix) {
        this.routingKeyPrefix = routingKeyPrefix;
    }

    public Map<String, String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(Map<String, String> routingKeys) {
        this.routingKeys = routingKeys;
    }
}
