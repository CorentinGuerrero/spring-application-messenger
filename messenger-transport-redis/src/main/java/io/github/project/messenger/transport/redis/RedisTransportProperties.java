package io.github.project.messenger.transport.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "messenger.transports.redis")
public class RedisTransportProperties {
    private boolean enabled = true;
    private String streamPrefix = "messenger";
    private Map<String, String> streams = new LinkedHashMap<>();
    private final Consumer consumer = new Consumer();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStreamPrefix() {
        return streamPrefix;
    }

    public void setStreamPrefix(String streamPrefix) {
        this.streamPrefix = streamPrefix;
    }

    public Map<String, String> getStreams() {
        return streams;
    }

    public void setStreams(Map<String, String> streams) {
        this.streams = streams;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public static final class Consumer {
        private boolean enabled;
        private List<String> streams = new ArrayList<>();
        private String group = "messenger";
        private String consumerName = "messenger-worker";
        private Duration pollTimeout = Duration.ofSeconds(1);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getStreams() {
            return streams;
        }

        public void setStreams(List<String> streams) {
            this.streams = streams;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getConsumerName() {
            return consumerName;
        }

        public void setConsumerName(String consumerName) {
            this.consumerName = consumerName;
        }

        public Duration getPollTimeout() {
            return pollTimeout;
        }

        public void setPollTimeout(Duration pollTimeout) {
            this.pollTimeout = pollTimeout;
        }
    }
}
