package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaApplicationContextArgumentResolver;

/**
 * Annotation to be used in conjunction with {@link PwaApplicationContextArgumentResolver} at the
 * controller level to ignore all application context checks.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PwaApplicationNoChecks {

}
