package uk.co.ogauthority.pwa.features.application.authorisation.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PwaResourceTypeCheck {

  PwaResourceType[] types();

}
