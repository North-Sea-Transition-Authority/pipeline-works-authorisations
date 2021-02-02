package uk.co.ogauthority.pwa.service.pwacontext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in conjunction with {@link uk.co.ogauthority.pwa.mvc.argresolvers.PwaContextArgumentResolver}
 * on controller methods to restrict processing of the method to users who have the required permissions for the case.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PwaPermissionCheck {

  PwaPermission[] permissions();

}
