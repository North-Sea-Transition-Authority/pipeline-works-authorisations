package uk.co.ogauthority.pwa.service.pwaapplications.contacts;

import java.util.Objects;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/* container class that links a user to an app and one role the user has in that app's contact team.*/
public class PwaApplicationContactRoleDto {

  private final int personId;

  private final int pwaApplicationId;

  private final PwaContactRole pwaContactRole;

  public PwaApplicationContactRoleDto(int personId, int pwaApplicationId,
                                      PwaContactRole pwaContactRole) {
    this.personId = personId;
    this.pwaApplicationId = pwaApplicationId;
    this.pwaContactRole = pwaContactRole;
  }

  public int getPersonId() {
    return personId;
  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public PwaContactRole getPwaContactRole() {
    return pwaContactRole;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaApplicationContactRoleDto that = (PwaApplicationContactRoleDto) o;
    return personId == that.personId
        && pwaApplicationId == that.pwaApplicationId
        && pwaContactRole == that.pwaContactRole;
  }

  @Override
  public int hashCode() {
    return Objects.hash(personId, pwaApplicationId, pwaContactRole);
  }
}
