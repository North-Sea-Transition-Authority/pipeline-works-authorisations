package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.JobKey.jobKey;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentReviewException;
import uk.co.ogauthority.pwa.features.appprocessing.issueconsent.IssueConsentEmailsService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.ConsentReviewDecision;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsentReviewServiceTest {

  @Mock
  private ConsentReviewRepository consentReviewRepository;

  @Mock
  private Clock clock;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private IssueConsentEmailsService issueConsentEmailsService;

  @Mock
  private Scheduler scheduler;

  @Mock
  private PwaApplicationEventService pwaApplicationEventService;

  private ConsentReviewService consentReviewService;

  @Captor
  private ArgumentCaptor<ConsentReview> consentReviewArgumentCaptor;

  @Captor
  private ArgumentCaptor<JobDetail> jobDetailCaptor;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final Person caseOfficerPerson = PersonTestUtil.createDefaultPerson();
  private final AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createPersonFrom(new PersonId(100))), Set.of());

  private final Instant fixedInstant = Instant.now();

  @BeforeEach
  void setUp() throws Exception {

    when(clock.instant()).thenReturn(fixedInstant);

    consentReviewService = new ConsentReviewService(
        consentReviewRepository,
        clock,
        pwaApplicationDetailService,
        workflowAssignmentService,
        camundaWorkflowService,
        issueConsentEmailsService,
        scheduler,
        pwaApplicationEventService);

  }

  @Test
  void startConsentReview_noOpenReviewExists() {

    consentReviewService.startConsentReview(detail, "my cover letter text", caseOfficerPerson);

    verify(consentReviewRepository, times(1)).save(consentReviewArgumentCaptor.capture());

    assertThat(consentReviewArgumentCaptor.getValue()).satisfies(review -> {
      assertThat(review.getPwaApplicationDetail()).isEqualTo(detail);
      assertThat(review.getCoverLetterText()).isEqualTo("my cover letter text");
      assertThat(review.getStatus()).isEqualTo(ConsentReviewStatus.OPEN);
      assertThat(review.getStartedByPersonId()).isEqualTo(caseOfficerPerson.getId());
      assertThat(review.getStartTimestamp()).isEqualTo(fixedInstant);
      assertThat(review.getEndedByPersonId()).isNull();
      assertThat(review.getEndTimestamp()).isNull();
      assertThat(review.getEndedReason()).isNull();
    });

  }


  @Test
  void startConsentReview_openReviewExists() {
    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    assertThrows(RuntimeException.class, () ->
      consentReviewService.startConsentReview(detail, "error going to happen", caseOfficerPerson));

  }

  @Test
  void getOpenConsentReview_openReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var optionalReview = consentReviewService.getOpenConsentReview(detail);

    assertThat(optionalReview).isPresent();

  }

  @Test
  void getOpenConsentReview_noOpenReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.APPROVED);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var optionalReview = consentReviewService.getOpenConsentReview(detail);

    assertThat(optionalReview).isEmpty();

  }

  @Test
  void returnToCaseOfficer_openReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    consentReviewService.returnToCaseOfficer(detail, "return reason", caseOfficerPerson, user);

    verify(consentReviewRepository, times(1)).save(consentReviewArgumentCaptor.capture());
    assertThat(consentReviewArgumentCaptor.getValue()).satisfies(consentReview -> {
      assertThat(consentReview.getStatus()).isEqualTo(ConsentReviewStatus.RETURNED);
      assertThat(consentReview.getEndTimestamp()).isEqualTo(clock.instant());
      assertThat(consentReview.getEndedByPersonId()).isEqualTo(user.getLinkedPerson().getId());
      assertThat(consentReview.getEndedReason()).isEqualTo("return reason");
    });

    // update app status
    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.CASE_OFFICER_REVIEW, user);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(detail.getPwaApplication(), ConsentReviewDecision.RETURN);
    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CONSENT_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

    verify(workflowAssignmentService, times(1)).assign(
        detail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        user.getLinkedPerson());

    verify(issueConsentEmailsService).sendConsentReviewReturnedEmail(
        detail, caseOfficerPerson, user.getLinkedPerson().getFullName(), "return reason");

  }

  @Test
  void returnToCaseOfficer_noOpenReview() {
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of());
    assertThrows(ConsentReviewException.class, () ->

      consentReviewService.returnToCaseOfficer(detail, "return reason", caseOfficerPerson, user));

  }

  @Test
  void findByPwaApplicationDetails() {

    var detailList = List.of(PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL), new PwaApplicationDetail());

    consentReviewService.findByPwaApplicationDetails(detailList);

    verify(consentReviewRepository, times(1)).findAllByPwaApplicationDetailIn(detailList);

  }

  @Test
  void scheduleConsentIssue_openReview() throws SchedulerException {

    var review = new ConsentReview();
    review.setStatus(ConsentReviewStatus.OPEN);

    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(review));

    consentReviewService.scheduleConsentIssue(detail, user);

    verify(pwaApplicationEventService, times(1)).clearEvents(detail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED);

    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.ISSUING_CONSENT, user);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(detail.getPwaApplication(), ConsentReviewDecision.APPROVE);
    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CONSENT_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

    verify(scheduler, times(1)).addJob(jobDetailCaptor.capture(), eq(false));

    var expectedJobKey = detail.getId() + "-" + clock.instant().getEpochSecond();

    assertThat(jobDetailCaptor.getValue()).satisfies(jobDetail -> {
      assertThat(jobDetail.getKey()).isEqualTo(jobKey(expectedJobKey, "PadConsentIssue"));
      assertThat(jobDetail.isDurable()).isTrue();
      assertThat(jobDetail.requestsRecovery()).isTrue();
      assertThat(jobDetail.getJobDataMap()).containsEntry("issuingWuaId", user.getWuaId());
    });

    var jobDetail = jobDetailCaptor.getValue();

    verify(scheduler, times(1)).triggerJob(jobDetail.getKey());

  }

  @Test
  void scheduleConsentIssue_noOpenReview() {
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of());
    assertThrows(ConsentReviewException.class, () ->

      consentReviewService.scheduleConsentIssue(detail, user));

  }

  @Test
  void approveConsentReview_openReview() {

    var review = new ConsentReview();
    review.setStatus(ConsentReviewStatus.OPEN);
    var approvalTime = Instant.now();

    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(review));

    consentReviewService.approveConsentReview(detail, user, approvalTime);

    verify(consentReviewRepository, times(1)).save(consentReviewArgumentCaptor.capture());

    assertThat(consentReviewArgumentCaptor.getValue()).satisfies(consentReview -> {
      assertThat(consentReview.getEndedByPersonId()).isEqualTo(user.getLinkedPerson().getId());
      assertThat(consentReview.getEndTimestamp()).isEqualTo(approvalTime);
      assertThat(consentReview.getStatus()).isEqualTo(ConsentReviewStatus.APPROVED);
    });

  }

  @Test
  void approveConsentReview_noOpenReview() {
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of());
    assertThrows(ConsentReviewException.class, () ->

      consentReviewService.approveConsentReview(detail, user, Instant.now()));

  }

  @Test
  void canStartConsentReview_canStart() {
    var startedReview = new ConsentReview();
    startedReview.setStatus(ConsentReviewStatus.APPROVED);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(startedReview));
    assertFalse(consentReviewService.areThereAnyOpenReviews(detail));
  }

  @Test
  void canStartConsentReview_cannotStart() {
    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));
    assertTrue(consentReviewService.areThereAnyOpenReviews(detail));
  }

}