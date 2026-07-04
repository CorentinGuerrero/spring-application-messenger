package io.github.project.messenger.handler;

import io.github.project.messenger.api.Spi;

import java.util.List;

@Spi
public interface HandlerRegistry {
    MessageHandler commandHandler(Class<?> commandType);

    MessageHandler queryHandler(Class<?> queryType);

    List<MessageHandler> eventHandlers(Class<?> eventType);
}
