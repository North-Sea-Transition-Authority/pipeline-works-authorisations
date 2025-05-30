package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.Set;

/**
 * Basic details of a PWA contact intended to avoid loading the whole object graph associated with the actual entity.
 */
public class PwaContactDto {

  private int pwaApplicationId;

  private Set<PwaContactRole> roles;

  private Integer personId;

  public PwaContactDto(int pwaApplicationId,
                       Integer personId,
                       Set<PwaContactRole> roles) {
    this.pwaApplicationId = pwaApplicationId;
    this.roles = roles;
    this.personId = personId;
  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public Set<PwaContactRole> getRoles() {
    return roles;
  }

  public Integer getPersonId() {
    return personId;
  }
}
