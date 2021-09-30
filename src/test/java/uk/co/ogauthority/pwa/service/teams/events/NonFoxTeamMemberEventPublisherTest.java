package uk.co.ogauthority.pwa.service.teams.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class NonFoxTeamMemberEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Captor
  private ArgumentCaptor<NonFoxTeamMemberModificationEvent> eventCaptor;

  private NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher;

  private final Person person = PersonTestUtil.createDefaultPerson();

  @Before
  public void setUp() throws Exception {

    nonFoxTeamMemberEventPublisher = new NonFoxTeamMemberEventPublisher(applicationEventPublisher);

  }

  @Test
  public void publishNonFoxTeamMemberAddedEvent() {

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberAddedEvent(person);

    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue()).satisfies(event -> {
      assertThat(event.getEventType()).isEqualTo(NonFoxTeamMemberModificationEvent.EventType.ADDED);
      assertThat(event.getPerson()).isEqualTo(person);
    });

  }

  @Test
  public void publishNonFoxTeamMemberRemovedEvent() {

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberRemovedEvent(person);

    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    assertThat(eventCaptor.getValue()).satisfies(event -> {
      assertThat(event.getEventType()).isEqualTo(NonFoxTeamMemberModificationEvent.EventType.REMOVED);
      assertThat(event.getPerson()).isEqualTo(person);
    });

  }

}