package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaApplicationContextArgumentResolver;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Annotation to be used in conjunction with {@link PwaApplicationContextArgumentResolver} on controller
 * methods to restrict processing of the method to applications at specific statuses.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PwaApplicationStatusCheck {

  PwaApplicationStatus[] statuses();

}
