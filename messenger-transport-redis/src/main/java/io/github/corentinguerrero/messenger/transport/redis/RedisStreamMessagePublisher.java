package io.github.corentinguerrero.messenger.transport.redis;

import java.util.Map;

public interface RedisStreamMessagePublisher {
    void publish(String stream, Map<String, Object> body);
}
