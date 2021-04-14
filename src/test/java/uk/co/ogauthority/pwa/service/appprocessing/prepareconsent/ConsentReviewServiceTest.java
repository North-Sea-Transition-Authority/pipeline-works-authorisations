package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentReviewException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.ConsentReviewReturnedEmailProps;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ConsentReviewRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.ConsentReviewDecision;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriterService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentReviewServiceTest {

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
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private ConsentWriterService consentWriterService;

  private ConsentReviewService consentReviewService;

  @Captor
  private ArgumentCaptor<ConsentReview> consentReviewArgumentCaptor;

  @Captor
  private ArgumentCaptor<ConsentReviewReturnedEmailProps> emailPropsCaptor;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final Person caseOfficerPerson = PersonTestUtil.createDefaultPerson();
  private final AuthenticatedUserAccount returningUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createPersonFrom(new PersonId(100))), Set.of());

  private final Instant fixedInstant = Instant.now();

  @Before
  public void setUp() throws Exception {

    when(clock.instant()).thenReturn(fixedInstant);
    when(emailCaseLinkService.generateCaseManagementLink(any())).thenCallRealMethod();

    consentReviewService = new ConsentReviewService(
        consentReviewRepository, clock, pwaApplicationDetailService,
        workflowAssignmentService, camundaWorkflowService, notifyService, emailCaseLinkService, pwaConsentService, consentWriterService);

  }

  @Test
  public void startConsentReview_noOpenReviewExists() {

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

  @Test(expected = RuntimeException.class)
  public void startConsentReview_openReviewExists() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    consentReviewService.startConsentReview(detail, "error going to happen", caseOfficerPerson);

  }

  @Test
  public void getOpenConsentReview_openReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var optionalReview = consentReviewService.getOpenConsentReview(detail);

    assertThat(optionalReview).isPresent();

  }

  @Test
  public void getOpenConsentReview_noOpenReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.APPROVED);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var optionalReview = consentReviewService.getOpenConsentReview(detail);

    assertThat(optionalReview).isEmpty();

  }

  @Test
  public void returnToCaseOfficer_openReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    consentReviewService.returnToCaseOfficer(detail, "return reason", caseOfficerPerson, returningUser);

    verify(consentReviewRepository, times(1)).save(consentReviewArgumentCaptor.capture());
    assertThat(consentReviewArgumentCaptor.getValue()).satisfies(consentReview -> {
      assertThat(consentReview.getStatus()).isEqualTo(ConsentReviewStatus.RETURNED);
      assertThat(consentReview.getEndTimestamp()).isEqualTo(clock.instant());
      assertThat(consentReview.getEndedByPersonId()).isEqualTo(returningUser.getLinkedPerson().getId());
      assertThat(consentReview.getEndedReason()).isEqualTo("return reason");
    });

    // update app status
    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.CASE_OFFICER_REVIEW, returningUser);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(detail.getPwaApplication(), ConsentReviewDecision.RETURN);
    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CONSENT_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

    verify(workflowAssignmentService, times(1)).assign(
        detail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        returningUser.getLinkedPerson());

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));

    assertThat(emailPropsCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName(),
        "RETURNING_PERSON_NAME", returningUser.getLinkedPerson().getFullName(),
        "APPLICATION_REFERENCE", detail.getPwaApplicationRef(),
        "RETURN_REASON", "return reason",
        "CASE_MANAGEMENT_LINK", emailCaseLinkService.generateCaseManagementLink(detail.getPwaApplication())
    ));

  }

  @Test(expected = ConsentReviewException.class)
  public void returnToCaseOfficer_noOpenReview() {

    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of());

    consentReviewService.returnToCaseOfficer(detail, "return reason", caseOfficerPerson, returningUser);

  }

  @Test
  public void findByPwaApplicationDetails() {

    var detailList = List.of(PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL), new PwaApplicationDetail());

    consentReviewService.findByPwaApplicationDetails(detailList);

    verify(consentReviewRepository, times(1)).findAllByPwaApplicationDetailIn(detailList);

  }

  @Test
  public void issueConsent_openReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var consent = new PwaConsent();
    consent.setReference("exampleRef");
    when(pwaConsentService.createConsent(detail.getPwaApplication())).thenReturn(consent);

    var issuedConsentDto = consentReviewService.issueConsent(detail, returningUser);

    verify(pwaConsentService, times(1)).createConsent(detail.getPwaApplication());
    verify(consentWriterService, times(1)).updateConsentedData(detail, consent);

    verify(consentReviewRepository, times(1)).save(consentReviewArgumentCaptor.capture());
    assertThat(consentReviewArgumentCaptor.getValue()).satisfies(consentReview -> {
      assertThat(consentReview.getStatus()).isEqualTo(ConsentReviewStatus.APPROVED);
      assertThat(consentReview.getEndTimestamp()).isEqualTo(clock.instant());
      assertThat(consentReview.getEndedByPersonId()).isEqualTo(returningUser.getLinkedPerson().getId());
      assertThat(consentReview.getEndedReason()).isNull();
    });

    assertThat(issuedConsentDto.getConsentReference()).isEqualTo(consent.getReference());

    // update app status
    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.COMPLETE, returningUser);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(detail.getPwaApplication(), ConsentReviewDecision.APPROVE);
    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CONSENT_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

    verify(workflowAssignmentService, times(1)).clearAssignments(detail.getPwaApplication());

  }

  @Test(expected = ConsentReviewException.class)
  public void issueConsent_noOpenReview() {

    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of());

    consentReviewService.issueConsent(detail, returningUser);

  }

}