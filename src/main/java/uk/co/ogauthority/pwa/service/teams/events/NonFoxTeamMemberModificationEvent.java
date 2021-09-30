package uk.co.ogauthority.pwa.service.teams.events;

import uk.co.ogauthority.pwa.energyportal.model.entity.Person;

public class NonFoxTeamMemberModificationEvent {

  public enum EventType {
    ADDED, REMOVED
  }

  private final Person person;
  private final EventType eventType;

  public NonFoxTeamMemberModificationEvent(Person person,
                                           EventType eventType) {
    this.person = person;
    this.eventType = eventType;
  }

  public Person getPerson() {
    return person;
  }

  public EventType getEventType() {
    return eventType;
  }

}
