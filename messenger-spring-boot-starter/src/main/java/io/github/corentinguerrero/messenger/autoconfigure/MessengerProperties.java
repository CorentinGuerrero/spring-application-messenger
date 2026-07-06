package io.github.corentinguerrero.messenger.autoconfigure;

import io.github.corentinguerrero.messenger.dispatch.EventErrorStrategy;
import io.github.corentinguerrero.messenger.routing.DefaultMessageRouter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "messenger")
public class MessengerProperties {
    private boolean enabled = true;
    private String defaultTransport = DefaultMessageRouter.DEFAULT_TRANSPORT;
    private final Scan scan = new Scan();
    private final Validation validation = new Validation();
    private final Observability observability = new Observability();
    private final Events events = new Events();
    private final Routing routing = new Routing();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultTransport() {
        return defaultTransport;
    }

    public void setDefaultTransport(String defaultTransport) {
        this.defaultTransport = defaultTransport;
    }

    public Scan scan() {
        return scan;
    }

    public Scan getScan() {
        return scan;
    }

    public Validation validation() {
        return validation;
    }

    public Validation getValidation() {
        return validation;
    }

    public Observability observability() {
        return observability;
    }

    public Observability getObservability() {
        return observability;
    }

    public Events events() {
        return events;
    }

    public Events getEvents() {
        return events;
    }

    public Routing routing() {
        return routing;
    }

    public Routing getRouting() {
        return routing;
    }

    public static final class Scan {
        private List<String> basePackages = new ArrayList<>();

        public List<String> getBasePackages() {
            return basePackages;
        }

        public void setBasePackages(List<String> basePackages) {
            this.basePackages = basePackages;
        }
    }

    public static final class Validation {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static final class Observability {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static final class Events {
        private EventErrorStrategy errorStrategy = EventErrorStrategy.FAIL_FAST;

        public EventErrorStrategy getErrorStrategy() {
            return errorStrategy;
        }

        public void setErrorStrategy(EventErrorStrategy errorStrategy) {
            this.errorStrategy = errorStrategy;
        }
    }

    public static final class Routing {
        private Map<String, String> commands = new LinkedHashMap<>();
        private Map<String, String> events = new LinkedHashMap<>();

        public Map<String, String> getCommands() {
            return commands;
        }

        public void setCommands(Map<String, String> commands) {
            this.commands = commands;
        }

        public Map<String, String> getEvents() {
            return events;
        }

        public void setEvents(Map<String, String> events) {
            this.events = events;
        }
    }
}
