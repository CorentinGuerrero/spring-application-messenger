package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Query;
import io.github.applicationmessenger.messenger.QueryBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class FakeQueryBus implements QueryBus {
    private final List<Query<?>> askedQueries = new ArrayList<>();
    private final Map<Class<?>, Function<Query<?>, Object>> handlers = new ConcurrentHashMap<>();

    public <Q extends Query<R>, R> FakeQueryBus whenAsking(Class<Q> queryType, Function<Q, R> handler) {
        Objects.requireNonNull(queryType, "queryType must not be null");
        Objects.requireNonNull(handler, "handler must not be null");
        handlers.put(queryType, query -> handler.apply(queryType.cast(query)));
        return this;
    }

    public <Q extends Query<R>, R> FakeQueryBus whenAskingReturn(Class<Q> queryType, R result) {
        return whenAsking(queryType, query -> result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <R> R ask(Query<R> query) {
        Objects.requireNonNull(query, "query must not be null");
        askedQueries.add(query);
        Function<Query<?>, Object> handler = handlers.get(query.getClass());
        if (handler == null) {
            return null;
        }
        return (R) handler.apply(query);
    }

    public synchronized List<Query<?>> askedQueries() {
        return List.copyOf(askedQueries);
    }

    public synchronized <Q extends Query<?>> List<Q> askedQueriesOfType(Class<Q> queryType) {
        return askedQueries.stream()
            .filter(queryType::isInstance)
            .map(queryType::cast)
            .toList();
    }

    public synchronized void clear() {
        askedQueries.clear();
        handlers.clear();
    }
}
