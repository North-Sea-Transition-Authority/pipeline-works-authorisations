package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatusHistory;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationGroupStatusHistoryRepository;
import uk.co.ogauthority.pwa.testutils.AsBuiltNotificationGroupStatusHistoryTestUtil;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationGroupStatusServiceTest {

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

  @BeforeEach
  void setup() {
    asBuiltNotificationGroupStatusService = new AsBuiltNotificationGroupStatusService(asBuiltNotificationGroupStatusHistoryRepository);
  }

  @Test
  void setInitialGroupStatus_createsNotStartedStatus() {

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
  void setGroupStatus_alreadyInProgress_statusNotChanged() {

    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup))
        .thenReturn(Optional.of(asBuiltNotificationGroupStatusHistory));

    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.IN_PROGRESS, person);
    verify(asBuiltNotificationGroupStatusHistoryRepository, never()).save(any());
  }

  @Test
  void setGroupStatus_alreadyInProgress_statusToComplete() {

    when(asBuiltNotificationGroupStatusHistoryRepository.findByAsBuiltNotificationGroupAndEndedTimestampIsNull(asBuiltNotificationGroup))
        .thenReturn(Optional.of(asBuiltNotificationGroupStatusHistory));

    asBuiltNotificationGroupStatusService.setGroupStatusIfNewOrChanged(asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE, person);
    verify(asBuiltNotificationGroupStatusHistoryRepository, times(2)).save(asBuiltNotificationGroupStatusHistoryArgumentCaptor.capture());
    var savedHistory = asBuiltNotificationGroupStatusHistoryArgumentCaptor.getValue();
    assertThat(savedHistory.getAsBuiltNotificationGroup()).isEqualTo(asBuiltNotificationGroup);
    assertThat(savedHistory.getStatus()).isEqualTo(AsBuiltNotificationGroupStatus.COMPLETE);
    assertThat(savedHistory.getCreatedByPersonId()).isEqualTo(person.getId());
    assertThat(savedHistory.getEndedByPersonId()).isEqualTo(null);
  }

  @Test
  void getAllNonCompleteAsBuiltNotificationGroups_correctlyCallsRepository() {
    asBuiltNotificationGroupStatusService.getAllNonCompleteAsBuiltNotificationGroups();
    verify(asBuiltNotificationGroupStatusHistoryRepository).findAllByEndedTimestampIsNullAndStatusIsNot(AsBuiltNotificationGroupStatus.COMPLETE);
  }

  @Test
  void isGroupStatusComplete_correctlyCallsRepository() {
    asBuiltNotificationGroupStatusService.isGroupStatusComplete(asBuiltNotificationGroup);
    verify(asBuiltNotificationGroupStatusHistoryRepository).findByAsBuiltNotificationGroupAndStatusAndEndedTimestampIsNull(
        asBuiltNotificationGroup, AsBuiltNotificationGroupStatus.COMPLETE);
  }

}
