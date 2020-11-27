package uk.co.ogauthority.pwa.service.appprocessing.options;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.workflow.GenericMessageEvent;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApprovalDeadlineHistoryRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
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

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;


  private ApproveOptionsService approveOptionsService;

  private PwaApplicationDetail pwaApplicationDetail;

  private Person person;
  private WebUserAccount webUserAccount;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    person = PersonTestUtil.createPersonFrom(PERSON_ID);
    webUserAccount = new WebUserAccount(1, person);

    approveOptionsService = new ApproveOptionsService(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService,
        padOptionConfirmedService,
        workflowAssignmentService,
        pwaApplicationDetailVersioningService,
        pwaApplicationRedirectService);

  }

  @Test
  public void approveOptions_serviceInteractions() {
    var instant = Instant.MAX;
    var newHistory = new OptionsApprovalDeadlineHistory();
    newHistory.setDeadlineDate(instant);
    when(optionsApprovalPersister.createInitialOptionsApproval(pwaApplicationDetail.getPwaApplication(), person, instant))
        .thenReturn(newHistory);

    // fake new version and just return existing one.
    when(pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, webUserAccount))
        .thenReturn(pwaApplicationDetail);

    approveOptionsService.approveOptions(pwaApplicationDetail, webUserAccount, instant);


    InOrder inOrder = Mockito.inOrder(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService,
        workflowAssignmentService,
        pwaApplicationDetailVersioningService);

    inOrder.verify(pwaApplicationDetailVersioningService, times(1))
        .createNewApplicationVersion(pwaApplicationDetail, webUserAccount);

    inOrder.verify(optionsApprovalPersister, times(1)).createInitialOptionsApproval(
        pwaApplicationDetail.getPwaApplication(), person, instant
    );

    inOrder.verify(optionsCaseManagementEmailService, times(1)).sendInitialOptionsApprovedEmail(
        pwaApplicationDetail, instant
    );

    inOrder.verify(workflowAssignmentService, times(1)).triggerWorkflowMessageAndAssertTaskExists(
        GenericMessageEvent.from(
            pwaApplicationDetail.getPwaApplication(),
            PwaApplicationWorkflowMessageEvents.OPTIONS_APPROVED.getMessageEventName()
        ),
        PwaApplicationWorkflowTask.UPDATE_APPLICATION
    );

    inOrder.verifyNoMoreInteractions();

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

  @Test
  public void getOptionsApprovalStatus_whenNotOptions(){
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    assertThat(approveOptionsService.getOptionsApprovalStatus(detail)).isEqualTo(OptionsApprovalStatus.NOT_APPLICABLE);

    verifyNoInteractions(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService,
        padOptionConfirmedService,
        workflowAssignmentService,
        pwaApplicationDetailVersioningService);

  }

  @Test
  public void getOptionsApprovalStatus_whenOptions_notApproved(){

    assertThat(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .isEqualTo(OptionsApprovalStatus.NOT_APPROVED);

    verify(optionsApplicationApprovalRepository, times(1))
        .findByPwaApplication(pwaApplicationDetail.getPwaApplication());

    verifyNoMoreInteractions(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService,
        padOptionConfirmedService,
        workflowAssignmentService,
        pwaApplicationDetailVersioningService);
  }

  @Test
  public void getOptionsApprovalStatus_whenOptions_approved_notResponded(){
    var approval = new OptionsApplicationApproval();

    when(optionsApplicationApprovalRepository.findByPwaApplication(any())).thenReturn(
        Optional.of(approval)
    );

    assertThat(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .isEqualTo(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    verify(optionsApplicationApprovalRepository, times(1))
        .findByPwaApplication(pwaApplicationDetail.getPwaApplication());

    verify(padOptionConfirmedService, times(1))
        .optionConfirmationExists(pwaApplicationDetail);

    verifyNoMoreInteractions(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService,
        padOptionConfirmedService,
        workflowAssignmentService,
        pwaApplicationDetailVersioningService);
  }

  @Test
  public void getOptionsApprovalStatus_whenOptions_approved_responded(){
    var approval = new OptionsApplicationApproval();

    when(optionsApplicationApprovalRepository.findByPwaApplication(any())).thenReturn(
        Optional.of(approval)
    );

    when(padOptionConfirmedService.optionConfirmationExists(any())).thenReturn(true);

    assertThat(approveOptionsService.getOptionsApprovalStatus(pwaApplicationDetail))
        .isEqualTo(OptionsApprovalStatus.APPROVED_RESPONDED);

    verify(optionsApplicationApprovalRepository, times(1))
        .findByPwaApplication(pwaApplicationDetail.getPwaApplication());

    verify(padOptionConfirmedService, times(1))
        .optionConfirmationExists(pwaApplicationDetail);

    verifyNoMoreInteractions(
        optionsApprovalPersister,
        optionsApplicationApprovalRepository,
        optionsApprovalDeadlineHistoryRepository,
        optionsCaseManagementEmailService,
        padOptionConfirmedService,
        workflowAssignmentService,
        pwaApplicationDetailVersioningService);
  }
}