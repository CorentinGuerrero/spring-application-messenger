package io.github.applicationmessenger.messenger.transport.redis;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

final class DefaultRedisStreamMessagePublisher implements RedisStreamMessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    DefaultRedisStreamMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publish(String stream, Map<String, Object> body) {
        MapRecord<String, String, Object> record = StreamRecords.newRecord()
            .in(stream)
            .ofMap(body);
        redisTemplate.opsForStream().add(record);
    }
}
