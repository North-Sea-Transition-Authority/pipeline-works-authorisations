package uk.co.ogauthority.pwa.integrations.energyportal.people.external;


public class PersonTestUtil {

  private PersonTestUtil() {
    // no instantiation
  }

  public static Person createPersonFrom(PersonId personId) {
    return createPersonFrom(
        personId,
        "email@email.com"
    );
  }

  public static Person createPersonFrom(PersonId personId, String email) {
    Person person = new Person();
    person.setId(personId.asInt());
    person.setForename("firstname");
    person.setSurname("surname");
    person.setEmailAddress(email);
    person.setTelephoneNo("0123456789");
    return person;

  }

  public static Person createPersonFrom(PersonId personId, String email, String forename) {
    Person person = new Person();
    person.setId(personId.asInt());
    person.setForename(forename);
    person.setSurname("surname");
    person.setEmailAddress(email);
    person.setTelephoneNo("0123456789");
    return person;
  }

  public static Person createDefaultPerson() {
    Person person = new Person();
    person.setId(100);
    person.setForename("firstname");
    person.setSurname("surname");
    person.setEmailAddress("email@email.com");
    person.setTelephoneNo("0123456789");
    return person;
  }

  public static Person createPersonWithNameFrom(PersonId personId) {
    return createPersonFrom(
        personId,
        "email@email.com",
        "surname"
    );
  }


}