package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ParallelConsentCheckLog;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.SendConsentForApprovalFormValidator;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.SendConsentForApprovalForm;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ParallelConsentCheckLogRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentDocumentServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ConsentDocumentEmailService consentDocumentEmailService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private SendForApprovalCheckerService sendforApprovalCheckerService;

  @Mock
  private SendConsentForApprovalFormValidator sendConsentForApprovalFormValidator;

  @Mock
  private ParallelConsentCheckLogRepository parallelConsentCheckLogRepository;

  private final Clock clock = Clock.systemUTC();

  private ConsentDocumentService consentDocumentService;

  private PwaApplicationDetail detail;

  private final Person person = PersonTestUtil.createDefaultPerson();
  private final AuthenticatedUserAccount authUser = new AuthenticatedUserAccount(new WebUserAccount(1, person), Set.of());

  @Before
  public void setUp() throws Exception {

    consentDocumentService = new ConsentDocumentService(
        pwaApplicationDetailService,
        consentDocumentEmailService,
        consentReviewService,
        camundaWorkflowService,
        sendforApprovalCheckerService,
        sendConsentForApprovalFormValidator,
        parallelConsentCheckLogRepository,
        clock);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void sendForApproval_verifyServiceCalls_noParallelConsentViews() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    consentDocumentService.sendForApproval(detail, "cover letter my text", authUser, List.of());

    verify(pwaApplicationDetailService, times(1)).updateStatus(detail, PwaApplicationStatus.CONSENT_REVIEW, authUser);

    verify(consentReviewService, times(1)).startConsentReview(detail, "cover letter my text", person);

    verify(consentDocumentEmailService, times(1)).sendConsentReviewStartedEmail(detail, person);

    verifyNoInteractions(parallelConsentCheckLogRepository);

    var workflowTaskInstance = new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW);
    verify(camundaWorkflowService, times(1)).completeTask(workflowTaskInstance);

  }

  @Test
  public void sendForApproval_verifyServiceCalls_withParallelConsentViews() {

    var parallelConsentViews = List.of(
        new ParallelConsentView(1, "", 1, PwaApplicationType.INITIAL, "", null, ""),
        new ParallelConsentView(2, "", 2, PwaApplicationType.CAT_1_VARIATION, "", null, ""),
        new ParallelConsentView(3, "", 3, PwaApplicationType.CAT_2_VARIATION, "", null, "")
    );

    var consentReview = new ConsentReview();
    consentReview.setId(10);
    when(consentReviewService.startConsentReview(any(), any(),any())).thenReturn(consentReview);

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    ArgumentCaptor<List<ParallelConsentCheckLog>> logCaptor = ArgumentCaptor.forClass(List.class);

    consentDocumentService.sendForApproval(detail, "cover letter my text", authUser, parallelConsentViews);

    verify(parallelConsentCheckLogRepository).saveAll(logCaptor.capture());
    assertThat(logCaptor.getValue()).hasSize(3);
    var consistentCheckInstant = logCaptor.getValue().get(0).getCheckConfirmedTimestamp();

    assertThat(logCaptor.getValue()).allSatisfy(parallelConsentCheckLog -> {
      assertThat(parallelConsentCheckLog.getCheckConfirmedTimestamp()).isEqualTo(consistentCheckInstant);
      assertThat(parallelConsentCheckLog.getPadConsentReviewId()).isEqualTo(consentReview.getId());
    })
        .anySatisfy(parallelConsentCheckLog -> assertThat(parallelConsentCheckLog.getPwaConsentId()).isEqualTo(1))
        .anySatisfy(parallelConsentCheckLog -> assertThat(parallelConsentCheckLog.getPwaConsentId()).isEqualTo(2))
        .anySatisfy(parallelConsentCheckLog -> assertThat(parallelConsentCheckLog.getPwaConsentId()).isEqualTo(3));
  }

  @Test
  public void validate_serviceInteractions(){

    var form = new SendConsentForApprovalForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var preApprovalChecksView = PreSendForApprovalChecksViewTestUtil.createNoFailedChecksView();
    var app = new PwaApplication();

    consentDocumentService.validateSendConsentFormUsingPreApprovalChecks(app, form, bindingResult, preApprovalChecksView);

    verify(sendConsentForApprovalFormValidator).validate(form, bindingResult, preApprovalChecksView, app);

  }

}