package io.github.applicationmessenger.messenger.routing;

import io.github.applicationmessenger.messenger.dispatch.BusType;
import io.github.applicationmessenger.messenger.transport.TransportNames;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DefaultMessageRouter implements MessageRouter {
    public static final String DEFAULT_TRANSPORT = TransportNames.SYNC;

    private final String defaultTransport;
    private final Map<String, String> commandRoutes;
    private final Map<String, String> eventRoutes;

    public DefaultMessageRouter(String defaultTransport, Map<String, String> commandRoutes, Map<String, String> queryRoutes, Map<String, String> eventRoutes) {
        this.defaultTransport = normalizeTransportName(defaultTransport == null || defaultTransport.isBlank() ? DEFAULT_TRANSPORT : defaultTransport);
        this.commandRoutes = normalizeRoutes(commandRoutes);
        this.eventRoutes = normalizeRoutes(eventRoutes);
    }

    public DefaultMessageRouter(String defaultTransport, Map<String, String> commandRoutes, Map<String, String> eventRoutes) {
        this(defaultTransport, commandRoutes, Map.of(), eventRoutes);
    }

    public static DefaultMessageRouter syncOnly() {
        return new DefaultMessageRouter(DEFAULT_TRANSPORT, Map.of(), Map.of());
    }

    @Override
    public MessageRoute routeFor(Object message, BusType busType) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(busType, "busType must not be null");
        Class<?> messageType = message.getClass();
        String transportName = routeMap(busType).get(messageType.getName());
        if (transportName == null) {
            transportName = routeMap(busType).get(messageType.getSimpleName());
        }
        if (transportName == null) {
            transportName = routeMap(busType).get("*");
        }
        if (transportName == null) {
            transportName = defaultTransport;
        }
        return new MessageRoute(busType, messageType, transportName);
    }

    private Map<String, String> routeMap(BusType busType) {
        return switch (busType) {
            case COMMAND -> commandRoutes;
            case QUERY -> Map.of();
            case EVENT -> eventRoutes;
        };
    }

    private static Map<String, String> normalizeRoutes(Map<String, String> routes) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (routes != null) {
            routes.forEach((messageType, transportName) -> normalized.put(messageType, normalizeTransportName(transportName)));
        }
        return Map.copyOf(normalized);
    }

    private static String normalizeTransportName(String transportName) {
        return transportName.toLowerCase(Locale.ROOT).trim();
    }
}
