package uk.co.ogauthority.pwa.controller.appprocessing.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

/**
 * Annotation to be used in conjunction with {@link uk.co.ogauthority.pwa.mvc.argresolvers.PwaAppProcessingContextArgumentResolver}
 * on controller methods to restrict processing of the method to users who have the required permissions for the case.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PwaAppProcessingPermissionCheck {

  PwaAppProcessingPermission[] permissions();

}
