package uk.co.ogauthority.pwa.controller;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.mvc.DefaultPageControllerAdvice;

@Import({
    DefaultPageControllerAdvice.class
})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDefaultPageControllerAdvice {
}