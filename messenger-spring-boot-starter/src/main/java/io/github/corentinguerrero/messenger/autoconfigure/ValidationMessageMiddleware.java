package io.github.corentinguerrero.messenger.autoconfigure;

import io.github.corentinguerrero.messenger.envelope.MessageEnvelope;
import io.github.corentinguerrero.messenger.handler.MessageHandler;
import io.github.corentinguerrero.messenger.middleware.MessageMiddleware;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.core.Ordered;

import java.util.Set;

public final class ValidationMessageMiddleware implements MessageMiddleware, Ordered {
    private final Validator validator;

    public ValidationMessageMiddleware(Validator validator) {
        this.validator = validator;
    }

    @Override
    public Object invoke(MessageEnvelope envelope, MessageHandler next) {
        Set<ConstraintViolation<Object>> violations = validator.validate(envelope.payload());
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return next.handle(envelope);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
