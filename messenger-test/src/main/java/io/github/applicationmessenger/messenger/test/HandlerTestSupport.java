package io.github.applicationmessenger.messenger.test;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.Event;
import io.github.applicationmessenger.messenger.Query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public final class HandlerTestSupport {
    private HandlerTestSupport() {
    }

    @SuppressWarnings("unchecked")
    public static <R> R invokeCommand(Object handler, Command<R> command) {
        return (R) invoke(handler, command);
    }

    @SuppressWarnings("unchecked")
    public static <R> R invokeQuery(Object handler, Query<R> query) {
        return (R) invoke(handler, query);
    }

    public static void invokeEvent(Object handler, Event event) {
        invoke(handler, event);
    }

    public static Object invoke(Object handler, Object message) {
        Objects.requireNonNull(handler, "handler must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Method method = handleMethod(handler.getClass(), message.getClass());
        try {
            method.setAccessible(true);
            return method.invoke(handler, message);
        } catch (InvocationTargetException exception) {
            throwUnchecked(exception.getTargetException());
            return null;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not invoke handler method " + method.toGenericString(), exception);
        }
    }

    private static Method handleMethod(Class<?> handlerType, Class<?> messageType) {
        return Arrays.stream(handlerType.getDeclaredMethods())
            .filter(method -> method.getName().equals("handle"))
            .filter(method -> method.getParameterCount() == 1)
            .filter(method -> method.getParameterTypes()[0].isAssignableFrom(messageType))
            .sorted(Comparator.comparing(Method::toGenericString))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No handle(...) method found on "
                + handlerType.getName() + " for message type " + messageType.getName()));
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwUnchecked(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
