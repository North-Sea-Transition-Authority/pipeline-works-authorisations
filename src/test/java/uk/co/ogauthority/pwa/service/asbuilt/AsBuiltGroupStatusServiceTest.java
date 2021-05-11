package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltGroupStatusServiceTest {

  @Mock
  private AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Captor
  private ArgumentCaptor<AsBuiltNotificationGroupStatusHistory> statusHistoryArgumentCaptor;


  private AsBuiltGroupStatusService asBuiltGroupStatusService;


  private AsBuiltNotificationGroup asBuiltGroup;

  private Person person;


  @Before
  public void setup() {

    asBuiltGroupStatusService = new AsBuiltGroupStatusService(asBuiltNotificationGroupStatusHistoryRepository, clock);

    asBuiltGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();
    person = PersonTestUtil.createDefaultPerson();

  }


  @Test
  public void setNewTipStatus_whenNoCurrentStatusExists() {

    asBuiltGroupStatusService.setNewTipStatus(asBuiltGroup, AsBuiltNotificationGroupStatus.NOT_STARTED, person);

    verify(asBuiltNotificationGroupStatusHistoryRepository).save(statusHistoryArgumentCaptor.capture());

    assertThat(statusHistoryArgumentCaptor.getAllValues()).hasOnlyOneElementSatisfying(statusHistory -> {
      assertThat(statusHistory.getStatus()).isEqualTo(AsBuiltNotificationGroupStatus.NOT_STARTED);
      assertThat(statusHistory.getCreatedTimestamp()).isEqualTo(clock.instant());
      assertThat(statusHistory.getCreatedByPersonId()).isEqualTo(person.getId());
      assertThat(statusHistory.getEndedByPersonId()).isNull();
      assertThat(statusHistory.getEndedTimestamp()).isNull();
    });

  }


  @Test
  public void setNewTipStatus_currentStatusExists() {

    var differentPerson = PersonTestUtil.createPersonFrom(new PersonId(1));

    var oldStatus = new AsBuiltNotificationGroupStatusHistory(
        asBuiltGroup,
        AsBuiltNotificationGroupStatus.NOT_STARTED,
        person.getId(),
        clock.instant().minus(2, ChronoUnit.HOURS));

    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(any()))
        .thenReturn(Optional.of(oldStatus));

    asBuiltGroupStatusService.setNewTipStatus(asBuiltGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS, differentPerson);

    verify(asBuiltNotificationGroupStatusHistoryRepository, times(2)).save(statusHistoryArgumentCaptor.capture());

    assertThat(statusHistoryArgumentCaptor.getAllValues()).hasSize(2);

    assertThat(statusHistoryArgumentCaptor.getAllValues())
        .element(0)
        .satisfies(statusHistory -> {
          assertThat(statusHistory.getStatus()).isEqualTo(oldStatus.getStatus());
          assertThat(statusHistory.getCreatedTimestamp()).isEqualTo(oldStatus.getCreatedTimestamp());
          assertThat(statusHistory.getCreatedByPersonId()).isEqualTo(oldStatus.getCreatedByPersonId());
          assertThat(statusHistory.getEndedByPersonId()).isEqualTo(differentPerson.getId());
          assertThat(statusHistory.getEndedTimestamp()).isEqualTo(clock.instant());
        });

    assertThat(statusHistoryArgumentCaptor.getAllValues())
        .element(1)
        .satisfies(statusHistory -> {
          assertThat(statusHistory.getStatus()).isEqualTo(AsBuiltNotificationGroupStatus.IN_PROGRESS);
          assertThat(statusHistory.getCreatedTimestamp()).isEqualTo(clock.instant());
          assertThat(statusHistory.getCreatedByPersonId()).isEqualTo(differentPerson.getId());
          assertThat(statusHistory.getEndedByPersonId()).isNull();
          assertThat(statusHistory.getEndedTimestamp()).isNull();
        });

  }
}