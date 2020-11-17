package uk.co.ogauthority.pwa.energyportal.model.entity;


public class PersonTestUtil {

  private PersonTestUtil() {
    // no instantiation
  }

  public static Person createPersonFrom(PersonId personId) {
    return new Person(
        personId.asInt(),
        "firstname",
        "surname",
        "email@email.com",
        "0123456789"
    );
  }

  public static Person createDefaultPerson() {
    return new Person(
        100,
        "firstname",
        "surname",
        "email@email.com",
        "0123456789"
    );
  }


}