package io.github.applicationmessenger.messenger.spring;

import io.github.applicationmessenger.messenger.api.PublicApi;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAnnotationApiContractTest {
    @Test
    void handlerAnnotationsStayRuntimeComponentAnnotations() {
        assertStableHandlerAnnotation(CommandHandler.class);
        assertStableHandlerAnnotation(QueryHandler.class);
        assertStableHandlerAnnotation(EventHandler.class);

        assertThat(TransactionalCommandHandler.class).hasAnnotation(Transactional.class);
        assertThat(TransactionalCommandHandler.class).hasAnnotation(CommandHandler.class);
    }

    private static void assertStableHandlerAnnotation(Class<?> annotationType) {
        assertThat(annotationType).hasAnnotation(PublicApi.class);
        assertThat(annotationType).hasAnnotation(Component.class);
        assertThat(annotationType.getAnnotation(Retention.class).value()).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(annotationType).hasAnnotation(Target.class);
    }
}
