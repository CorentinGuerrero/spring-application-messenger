package io.github.corentinguerrero.messenger.spring;

import io.github.corentinguerrero.messenger.api.PublicApi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Transactional
@CommandHandler
@PublicApi
public @interface TransactionalCommandHandler {
}
