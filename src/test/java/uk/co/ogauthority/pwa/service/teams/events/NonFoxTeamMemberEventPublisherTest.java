package uk.co.ogauthority.pwa.service.teams.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;

@ExtendWith(MockitoExtension.class)
class NonFoxTeamMemberEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Captor
  private ArgumentCaptor<NonFoxTeamMemberModificationEvent> eventCaptor;

  private NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher;

  private final Person person = PersonTestUtil.createDefaultPerson();

  @BeforeEach
  void setUp() throws Exception {

    nonFoxTeamMemberEventPublisher = new NonFoxTeamMemberEventPublisher(applicationEventPublisher);

  }

  @Test
  void publishNonFoxTeamMemberAddedEvent() {

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberAddedEvent(person);

    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue()).satisfies(event -> {
      assertThat(event.getEventType()).isEqualTo(NonFoxTeamMemberModificationEvent.EventType.ADDED);
      assertThat(event.getPerson()).isEqualTo(person);
    });

  }

  @Test
  void publishNonFoxTeamMemberRemovedEvent() {

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberRemovedEvent(person);

    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue()).satisfies(event -> {
      assertThat(event.getEventType()).isEqualTo(NonFoxTeamMemberModificationEvent.EventType.REMOVED);
      assertThat(event.getPerson()).isEqualTo(person);
    });

  }

}