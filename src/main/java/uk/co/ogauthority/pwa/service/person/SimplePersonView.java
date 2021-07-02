package uk.co.ogauthority.pwa.service.person;

import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;

/**
 * Basic view of a person useful for View objects
 */
public class SimplePersonView {

  private final PersonId personId;
  private final String name;
  private final String email;

  SimplePersonView(PersonId personId, String name, String email) {
    this.personId = personId;
    this.name = name;
    this.email = email;
  }

  public PersonId getPersonId() {
    return personId;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }
}
