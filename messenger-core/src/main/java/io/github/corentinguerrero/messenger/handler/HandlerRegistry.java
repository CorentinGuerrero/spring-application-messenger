package io.github.corentinguerrero.messenger.handler;

import io.github.corentinguerrero.messenger.api.Spi;

import java.util.List;

@Spi
public interface HandlerRegistry {
    MessageHandler commandHandler(Class<?> commandType);

    MessageHandler queryHandler(Class<?> queryType);

    List<MessageHandler> eventHandlers(Class<?> eventType);
}
