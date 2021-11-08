package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;
import uk.co.ogauthority.pwa.testutils.AsBuiltNotificationGroupStatusHistoryTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationGroupStatusServiceTest {

  @Mock
  private AsBuiltNotificationGroupStatusHistoryRepository asBuiltNotificationGroupStatusHistoryRepository;

  private AsBuiltNotificationGroupStatusService asBuiltNotificationGroupStatusService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationGroupStatusHistory> asBuiltNotificationGroupStatusHistoryArgumentCaptor;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil.createDefaultGroupWithConsent();
  private final Person person = PersonTestUtil.createPersonFrom(new PersonId(10));
  private final AsBuiltNotificationGroupStatusHistory asBuiltNotificationGroupStatusHistory =
      AsBuiltNotificationGroupStatusHistoryTestUtil.createAsBuiltStatusHistory_withNotificationGroup(asBuiltNotificationGroup,
          AsBuiltNotificationGroupStatus.IN_PROGRESS);

  @Before
  public void setup() {
    asBuiltNotificationGroupStatusService = new AsBuiltNotificationGroupStatusService(asBuiltNotificationGroupStatusHistoryRepository);

    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup))
        .thenReturn(Optional.of(asBuiltNotificationGroupStatusHistory));
  }

  @Test
  public void setInitialGroupStatus_createsNotStartedStatus() {
    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup))
        .thenReturn(Optional.empty());

    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.NOT_STARTED, person);

    verify(asBuiltNotificationGroupStatusHistoryRepository).save(asBuiltNotificationGroupStatusHistoryArgumentCaptor.capture());

    assertThat(asBuiltNotificationGroupStatusHistoryArgumentCaptor.getAllValues()).hasOnlyOneElementSatisfying(statusHistory -> {
      assertThat(statusHistory.getStatus()).isEqualTo(AsBuiltNotificationGroupStatus.NOT_STARTED);
      assertThat(statusHistory.getCreatedByPersonId()).isEqualTo(person.getId());
      assertThat(statusHistory.getEndedByPersonId()).isNull();
      assertThat(statusHistory.getEndedTimestamp()).isNull();
    });

  }

  @Test
  public void setGroupStatus_alreadyInProgress_statusNotChanged() {
    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS, person);
    verify(asBuiltNotificationGroupStatusHistoryRepository, never()).save(any());
  }

  @Test
  public void setGroupStatus_alreadyInProgress_statusToComplete() {
    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE, person);
    verify(asBuiltNotificationGroupStatusHistoryRepository, times(2)).save(asBuiltNotificationGroupStatusHistoryArgumentCaptor.capture());
    var savedHistory = asBuiltNotificationGroupStatusHistoryArgumentCaptor.getValue();
    assertThat(savedHistory.getAsBuiltNotificationGroup()).isEqualTo(asBuiltNotificationGroup);
    assertThat(savedHistory.getStatus()).isEqualTo(AsBuiltNotificationGroupStatus.COMPLETE);
    assertThat(savedHistory.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(savedHistory.getEndedByPersonId()).isEqualTo(null);
  }

  @Test
  public void getAllNonCompleteAsBuiltNotificationGroups_correctlyCallsRepository() {
    asBuiltNotificationGroupStatusService.getAllNonCompleteAsBuiltNotificationGroups();
    verify(asBuiltNotificationGroupStatusHistoryRepository).findAllByEndedTimestampIsNullAndStatusIsNot(AsBuiltNotificationGroupStatus.COMPLETE);
  }

  @Test
  public void isGroupStatusComplete_correctlyCallsRepository() {
    asBuiltNotificationGroupStatusService.isGroupStatusComplete(asBuiltNotificationGroup);
    verify(asBuiltNotificationGroupStatusHistoryRepository).findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
        asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE);
  }

}
