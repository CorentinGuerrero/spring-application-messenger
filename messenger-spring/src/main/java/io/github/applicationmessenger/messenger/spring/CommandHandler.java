package io.github.applicationmessenger.messenger.spring;

import io.github.applicationmessenger.messenger.api.PublicApi;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@PublicApi
public @interface CommandHandler {
}
