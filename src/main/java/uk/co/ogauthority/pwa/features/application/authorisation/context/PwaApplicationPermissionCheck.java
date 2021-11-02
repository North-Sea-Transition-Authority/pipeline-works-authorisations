package uk.co.ogauthority.pwa.features.application.authorisation.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;

/**
 * Annotation to be used in conjunction with {@link PwaApplicationContextArgumentResolver} on controller
 * methods to restrict processing of the method to users who have all defined permissions on the application.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PwaApplicationPermissionCheck {

  PwaApplicationPermission[] permissions();

}
