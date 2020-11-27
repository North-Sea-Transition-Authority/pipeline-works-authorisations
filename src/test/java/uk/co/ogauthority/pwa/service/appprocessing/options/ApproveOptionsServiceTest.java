package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApproveOptionsServiceTest {

  private static final String NOTE = "A Note";

  private static final PersonId PERSON_ID = new PersonId(1);

  @Mock
  private OptionsApprovalPersister optionsApprovalPersister;

  @Mock
  private OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  @Mock
  private OptionsApprovalDeadlineHistoryRepository optionsApprovalDeadlineHistoryRepository;

  @Mock
  private OptionsCaseManagementEmailService optionsCaseManagementEmailService;


  private ApproveOptionsService approveOptionsService;

  private PwaApplicationDetail pwaApplicationDetail;

  private Person person;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    person = PersonTestUtil.createPersonFrom(PERSON_ID);

    approveOptionsService = new ApproveOptionsService(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService);

  }

  @Test
  public void approveOptions_serviceInteractions() {
    var instant = Instant.MAX;
    approveOptionsService.approveOptions(pwaApplicationDetail, person, instant);

    verify(optionsApprovalPersister, times(1)).createInitialOptionsApproval(
        pwaApplicationDetail.getPwaApplication(), person, instant
    );

    verify(optionsCaseManagementEmailService, times(1)).sendInitialOptionsApprovedEmail(
        pwaApplicationDetail
    );

  }


  @Test
  public void changeOptionsApprovalDeadline_serviceInteractions(){

    var deadlineAsInstant = Instant.MAX;

    var approval = new OptionsApplicationApproval();
    var fakeNewHistory = new OptionsApprovalDeadlineHistory();
    fakeNewHistory.setDeadlineDate(deadlineAsInstant);

    when(optionsApplicationApprovalRepository.findByPwaApplication(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.of(approval));

    when(optionsApprovalPersister.createTipDeadlineHistoryItem(any(), any(), any(), any()))
        .thenReturn(fakeNewHistory);

    approveOptionsService.changeOptionsApprovalDeadline(pwaApplicationDetail, person, deadlineAsInstant, NOTE);

    InOrder verifyOrder = Mockito.inOrder(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService
    );

    verifyOrder.verify(optionsApprovalPersister, times(1)).endTipDeadlineHistoryItem(approval);
    verifyOrder.verify(optionsApprovalPersister, times(1)).createTipDeadlineHistoryItem(approval, person, deadlineAsInstant, NOTE);
    verifyOrder.verify(optionsCaseManagementEmailService, times(1)).sendOptionsDeadlineChangedEmail(
        pwaApplicationDetail, deadlineAsInstant
    );
    verifyOrder.verifyNoMoreInteractions();

  }

  @Test
  public void getOptionsApprovalDeadlineViewOrError_whenApproved_mapsData() {
    var approvedByPersonId = new PersonId(1);
    var updatedByPersonId = new PersonId(2);
    var approvedInstant = Instant.MIN;
    var updatedClock = Clock.fixed(Instant.MAX, ZoneId.systemDefault());

    var deadline = Instant.now();

    var approval = OptionsApplicationApproval.from(
        approvedByPersonId, approvedInstant, pwaApplicationDetail.getPwaApplication()
    );

    var deadlineHistory = OptionsApprovalDeadlineHistory.createTipFrom(
        approval, updatedByPersonId, updatedClock, deadline, NOTE
    );

    when(optionsApprovalDeadlineHistoryRepository.findByOptionsApplicationApproval_PwaApplicationAndTipFlagIsTrue(
        pwaApplicationDetail.getPwaApplication()))
    .thenReturn(Optional.of(deadlineHistory));

    var view = approveOptionsService.getOptionsApprovalDeadlineViewOrError(pwaApplicationDetail.getPwaApplication());

    assertThat(view.getApprovedByPersonId()).isEqualTo(approvedByPersonId);
    assertThat(view.getApprovedInstant()).isEqualTo(approvedInstant);
    assertThat(view.getDeadlineUpdatedByPersonId()).isEqualTo(updatedByPersonId);
    assertThat(view.getDeadlineUpdatedInstant()).isEqualTo(updatedClock.instant());
    assertThat(view.getDeadlineInstant()).isEqualTo(deadline);
    assertThat(view.getUpdateNote()).isEqualTo(NOTE);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getOptionsApprovalDeadlineViewOrError_whenNoOptionsApproval() {

    when(optionsApprovalDeadlineHistoryRepository.findByOptionsApplicationApproval_PwaApplicationAndTipFlagIsTrue(
        pwaApplicationDetail.getPwaApplication()))
        .thenReturn(Optional.empty());

    approveOptionsService.getOptionsApprovalDeadlineViewOrError(pwaApplicationDetail.getPwaApplication());

  }
}