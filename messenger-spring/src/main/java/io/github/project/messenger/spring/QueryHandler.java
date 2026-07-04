package io.github.project.messenger.spring;

import io.github.project.messenger.api.PublicApi;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@PublicApi
public @interface QueryHandler {
}
