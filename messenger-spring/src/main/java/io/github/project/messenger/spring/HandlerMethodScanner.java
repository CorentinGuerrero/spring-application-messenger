package io.github.project.messenger.spring;

import io.github.project.messenger.Command;
import io.github.project.messenger.Event;
import io.github.project.messenger.Query;
import io.github.project.messenger.exception.InvalidHandlerSignatureException;
import io.github.project.messenger.exception.MessageHandlingException;
import io.github.project.messenger.handler.InMemoryHandlerRegistry;
import io.github.project.messenger.handler.MessageHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class HandlerMethodScanner {
    private final ApplicationContext applicationContext;

    public HandlerMethodScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public InMemoryHandlerRegistry scan() {
        InMemoryHandlerRegistry.Builder builder = InMemoryHandlerRegistry.builder();

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> candidateType = applicationContext.getType(beanName);
            if (candidateType == null || !isMessengerHandler(candidateType)) {
                continue;
            }

            Class<?> targetClass = candidateType;

            if (isCommandHandler(targetClass)) {
                registerHandlers(beanName, targetClass, HandlerKind.COMMAND, builder);
            }
            if (isQueryHandler(targetClass)) {
                registerHandlers(beanName, targetClass, HandlerKind.QUERY, builder);
            }
            if (isEventHandler(targetClass)) {
                registerHandlers(beanName, targetClass, HandlerKind.EVENT, builder);
            }
        }

        return builder.build();
    }

    private static boolean isMessengerHandler(Class<?> targetClass) {
        return isCommandHandler(targetClass) || isQueryHandler(targetClass) || isEventHandler(targetClass);
    }

    private static boolean isCommandHandler(Class<?> targetClass) {
        return AnnotationUtils.findAnnotation(targetClass, CommandHandler.class) != null
            || AnnotationUtils.findAnnotation(targetClass, TransactionalCommandHandler.class) != null;
    }

    private static boolean isQueryHandler(Class<?> targetClass) {
        return AnnotationUtils.findAnnotation(targetClass, QueryHandler.class) != null;
    }

    private static boolean isEventHandler(Class<?> targetClass) {
        return AnnotationUtils.findAnnotation(targetClass, EventHandler.class) != null;
    }

    private void registerHandlers(String beanName, Class<?> targetClass, HandlerKind kind, InMemoryHandlerRegistry.Builder builder) {
        List<Method> methods = Arrays.stream(ReflectionUtils.getUniqueDeclaredMethods(targetClass))
            .filter(method -> method.getName().equals("handle"))
            .sorted(Comparator.comparing(Method::toGenericString))
            .toList();

        if (methods.isEmpty()) {
            throw invalid(targetClass, "must declare at least one handle(...) method");
        }

        for (Method method : methods) {
            Class<?> messageType = validateMethod(targetClass, method, kind);
            MessageHandler handler = envelope -> {
                Object bean = applicationContext.getBean(beanName);
                Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
                return invoke(bean, invocableMethod, envelope.payload());
            };

            switch (kind) {
                case COMMAND -> builder.commandHandler(messageType, handler);
                case QUERY -> builder.queryHandler(messageType, handler);
                case EVENT -> builder.eventHandler(messageType, handler);
            }
        }
    }

    private static Class<?> validateMethod(Class<?> targetClass, Method method, HandlerKind kind) {
        if (method.getParameterCount() != 1) {
            throw invalid(targetClass, method, "must have exactly one message parameter");
        }

        Class<?> messageType = method.getParameterTypes()[0];
        Class<?> expectedType = kind.messageMarker();
        if (!expectedType.isAssignableFrom(messageType)) {
            throw invalid(targetClass, method, "parameter must implement " + expectedType.getSimpleName());
        }

        if ((kind == HandlerKind.COMMAND || kind == HandlerKind.QUERY) && method.getReturnType().equals(Void.TYPE)) {
            throw invalid(targetClass, method, kind.name().toLowerCase() + " handler must return a result");
        }

        validateReturnType(targetClass, method, messageType, kind);

        if (kind == HandlerKind.EVENT && !method.getReturnType().equals(Void.TYPE)) {
            throw invalid(targetClass, method, "event handler must return void");
        }

        return messageType;
    }

    private static void validateReturnType(Class<?> targetClass, Method method, Class<?> messageType, HandlerKind kind) {
        if (kind == HandlerKind.EVENT) {
            return;
        }

        Class<?> contractType = ResolvableType.forClass(messageType)
            .as(kind.messageMarker())
            .getGeneric(0)
            .resolve(Object.class);

        if (contractType.equals(Object.class)) {
            return;
        }

        Class<?> returnType = method.getReturnType();
        if (!contractType.isAssignableFrom(returnType)) {
            throw invalid(targetClass, method, "return type " + returnType.getName()
                + " is not compatible with " + kind.messageMarker().getSimpleName()
                + "<" + contractType.getName() + ">");
        }
    }

    private static Object invoke(Object bean, Method method, Object payload) {
        try {
            ReflectionUtils.makeAccessible(method);
            return method.invoke(bean, payload);
        } catch (InvocationTargetException exception) {
            throwUnchecked(exception.getTargetException());
            return null;
        } catch (IllegalAccessException exception) {
            throw new MessageHandlingException("Could not invoke handler method " + method.toGenericString(), exception);
        }
    }

    private static InvalidHandlerSignatureException invalid(Class<?> targetClass, String reason) {
        return new InvalidHandlerSignatureException("Invalid handler " + targetClass.getName() + ": " + reason);
    }

    private static InvalidHandlerSignatureException invalid(Class<?> targetClass, Method method, String reason) {
        return new InvalidHandlerSignatureException("Invalid handler method " + targetClass.getName() + "#" + method.getName() + ": " + reason);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwUnchecked(Throwable throwable) throws E {
        throw (E) throwable;
    }

    private enum HandlerKind {
        COMMAND(Command.class),
        QUERY(Query.class),
        EVENT(Event.class);

        private final Class<?> messageMarker;

        HandlerKind(Class<?> messageMarker) {
            this.messageMarker = messageMarker;
        }

        Class<?> messageMarker() {
            return messageMarker;
        }
    }
}
