package uk.co.ogauthority.pwa.service.teams.events;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

@Component
public class NonFoxTeamMemberEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public NonFoxTeamMemberEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Transactional
  public void publishNonFoxTeamMemberAddedEvent(Person person) {
    publishEvent(person, NonFoxTeamMemberModificationEvent.EventType.ADDED);
  }

  private void publishEvent(Person person,
                            NonFoxTeamMemberModificationEvent.EventType eventType) {

    var event = new NonFoxTeamMemberModificationEvent(person, eventType);
    applicationEventPublisher.publishEvent(event);

  }

  @Transactional
  public void publishNonFoxTeamMemberRemovedEvent(Person person) {
    publishEvent(person, NonFoxTeamMemberModificationEvent.EventType.REMOVED);
  }

}
