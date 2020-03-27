package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Annotation to be used in conjunction with {@link uk.co.ogauthority.pwa.mvc.PwaApplicationContextArgumentResolver} on controller
 * methods to restrict processing of the method to applications at a specific status.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PwaApplicationStatusCheck {

  PwaApplicationStatus status();

}
