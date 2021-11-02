package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;


import java.util.EnumSet;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;

public final class PwaContactTestUtil {

  private PwaContactTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PwaContact createBasicAllRoleContact(Person person){
    return new PwaContact(null, person, EnumSet.allOf(PwaContactRole.class));
  }
}