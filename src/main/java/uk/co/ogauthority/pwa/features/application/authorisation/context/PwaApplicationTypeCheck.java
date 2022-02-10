package uk.co.ogauthority.pwa.features.application.authorisation.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

/**
 * Annotation to be used in conjunction with {@link PwaApplicationContextArgumentResolver} at the
 * controller level to restrict all routes within that controller to applications that match one of the defined types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PwaApplicationTypeCheck {

  PwaApplicationType[] types();

}
