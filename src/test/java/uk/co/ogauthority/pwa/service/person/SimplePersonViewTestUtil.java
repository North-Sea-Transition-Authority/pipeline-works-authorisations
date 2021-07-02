package uk.co.ogauthority.pwa.service.person;

import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;

public final class SimplePersonViewTestUtil {

  private SimplePersonViewTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static SimplePersonView createView(PersonId personId){
    return new SimplePersonView(personId, "Person Name", "person.name@example.com");
  }

}