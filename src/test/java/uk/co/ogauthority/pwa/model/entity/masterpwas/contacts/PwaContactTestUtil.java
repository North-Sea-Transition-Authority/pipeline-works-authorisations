package uk.co.ogauthority.pwa.model.entity.masterpwas.contacts;


import java.util.EnumSet;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

public final class PwaContactTestUtil {

  private PwaContactTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PwaContact createBasicAllRoleContact(Person person){
    return new PwaContact(null, person, EnumSet.allOf(PwaContactRole.class));
  }
}