package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Annotation to be used in conjunction with {@link uk.co.ogauthority.pwa.mvc.PwaApplicationContextArgumentResolver} at the
 * controller level to restrict all routes within that controller to applications that match one of the defined types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PwaApplicationTypeCheck {

  PwaApplicationType[] types();

}
