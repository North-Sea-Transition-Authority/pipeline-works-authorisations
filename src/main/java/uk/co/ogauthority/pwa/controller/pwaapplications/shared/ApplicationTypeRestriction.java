package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationTypeRestriction {
  PwaApplicationType[] value();
}
