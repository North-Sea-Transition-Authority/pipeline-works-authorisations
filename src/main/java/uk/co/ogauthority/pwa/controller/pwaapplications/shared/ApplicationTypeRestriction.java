package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationTypeRestriction {
  PwaApplicationType[] value();
}
