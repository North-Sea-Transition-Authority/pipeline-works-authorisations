package uk.co.ogauthority.pwa.integrations.energyportal.people.external;

public final class SimplePersonViewTestUtil {

  private SimplePersonViewTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static SimplePersonView createView(PersonId personId){
    return new SimplePersonView(personId, "Person Name", "person.name@example.com");
  }

}