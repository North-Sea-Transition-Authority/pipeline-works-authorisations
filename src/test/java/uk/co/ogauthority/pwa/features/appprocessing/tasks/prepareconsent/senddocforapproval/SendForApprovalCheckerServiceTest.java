package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.AppProcessingTaskWarningService;
import uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.NonBlockingWarningPage;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.AppProcessingTaskWarningTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.documents.DocumentViewService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class SendForApprovalCheckerServiceTest {

  private static final Instant APP_CREATION_INSTANT = LocalDateTime.of(2021, 1 ,1 ,0,0,0)
      .toInstant(ZoneOffset.UTC);

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private MasterPwaService masterPwaService;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private AppProcessingTaskWarningService appProcessingTaskWarningService;

  @Mock
  private DocumentViewService documentViewService;

  private SendForApprovalCheckerService sendforApprovalCheckerService;

  private PwaApplicationDetail detail;

  private MasterPwaDetail masterPwaDetail;

  @Before
  public void setUp() throws Exception {
    sendforApprovalCheckerService = new SendForApprovalCheckerService(
        applicationUpdateRequestService,
        consultationRequestService,
        publicNoticeService,
        documentInstanceService,
        masterPwaService,
        pwaConsentService,
        appProcessingTaskWarningService,
        documentViewService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.getPwaApplication().setApplicationCreatedTimestamp(APP_CREATION_INSTANT);

    masterPwaDetail = new MasterPwaDetail(detail.getMasterPwa(), MasterPwaDetailStatus.APPLICATION, "reference", Instant.now());
    when(masterPwaService.getCurrentDetailOrThrow(detail.getMasterPwa())).thenReturn(masterPwaDetail);

    // detail is satisfactory
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    // no updates
    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(false);
    // no consultations
    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of());
    // no public notice
    when(publicNoticeService.publicNoticeInProgress(detail.getPwaApplication())).thenReturn(false);
    // doc clauses exist
    when(documentViewService.documentViewHasClauses(any())).thenReturn(true);
    // doc clauses don't have manual merge data
    when(documentViewService.documentViewContainsManualMergeData(any())).thenReturn(false);

  }

  @Test
  public void getPreSendForApprovalChecksView_latestVersionNotSatisfactory() {

    detail.setConfirmedSatisfactoryTimestamp(null);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(
              SendConsentForApprovalRequirement.LATEST_APP_VERSION_IS_SATISFACTORY);
        });


  }

  @Test
  public void getPreSendForApprovalChecksView_updateInProgress() {

    when(applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)).thenReturn(true);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(SendConsentForApprovalRequirement.NO_UPDATE_IN_PROGRESS);
        });

  }

  @Test
  public void getPreSendForApprovalChecksView_consultationInProgress() {

    when(consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()))
        .thenReturn(List.of(new ConsultationRequest()));

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(SendConsentForApprovalRequirement.NO_CONSULTATION_IN_PROGRESS);
        });

  }

  @Test
  public void getPreSendForApprovalChecksView_publicNoticeInProgress() {

    when(publicNoticeService.publicNoticeInProgress(detail.getPwaApplication())).thenReturn(true);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(SendConsentForApprovalRequirement.NO_PUBLIC_NOTICE_IN_PROGRESS);
        });


  }

  @Test
  public void getPreSendForApprovalChecksView_noDocumentClauses() {

    when(documentViewService.documentViewHasClauses(any())).thenReturn(false);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(SendConsentForApprovalRequirement.DOCUMENT_HAS_CLAUSES);
        });

  }

  @Test
  public void getPreSendForApprovalChecksView_manualMergeDataPresent() {

    when(documentViewService.documentViewContainsManualMergeData(any())).thenReturn(true);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(SendConsentForApprovalRequirement.DOCUMENT_HAS_NO_MANUAL_MERGE_DATA);
        });

  }

  @Test
  public void getPreSendForApprovalChecksView_depositOnNonConsentedPwa() {

    detail.getPwaApplication().setApplicationType(PwaApplicationType.DEPOSIT_CONSENT);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .hasOnlyOneElementSatisfying(failedSendForApprovalCheck -> {
          assertThat(failedSendForApprovalCheck.getSendConsentForApprovalRequirement()).isEqualTo(SendConsentForApprovalRequirement.MASTER_PWA_IS_NOT_CONSENTED);
        });
  }

  @Test
  public void getPreSendForApprovalChecksView_depositOnConsentedPwa() {

    detail.getPwaApplication().setApplicationType(PwaApplicationType.DEPOSIT_CONSENT);
    masterPwaDetail.setMasterPwaDetailStatus(MasterPwaDetailStatus.CONSENTED);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks()).isEmpty();
  }

  @Test
  public void getPreSendForApprovalChecksView_parallelConsentFound() {

    detail.getPwaApplication().setApplicationType(PwaApplicationType.DEPOSIT_CONSENT);
    masterPwaDetail.setMasterPwaDetailStatus(MasterPwaDetailStatus.CONSENTED);

    var application = new PwaApplication();
    application.setId(5);
    application.setApplicationType(PwaApplicationType.INITIAL);
    application.setAppReference("appReference");
    var consent = new PwaConsent();
    consent.setId(10);
    consent.setReference("consentReference");
    consent.setConsentInstant(APP_CREATION_INSTANT.plus(10, ChronoUnit.DAYS));
    consent.setSourcePwaApplication(application);

    when(pwaConsentService.getPwaConsentsWhereConsentInstantAfter(detail.getMasterPwa(), APP_CREATION_INSTANT))
        .thenReturn(List.of(consent));

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks()).isEmpty();
    assertThat(preSendApprovalCheckView.getParallelConsentViews()).hasOnlyOneElementSatisfying(parallelConsentView -> {
      assertThat(parallelConsentView.getConsentInstant()).isEqualTo(consent.getConsentInstant());
      assertThat(parallelConsentView.getPwaConsentId()).isEqualTo(consent.getId());
      assertThat(parallelConsentView.getApplicationReference()).isEqualTo(application.getAppReference());
      assertThat(parallelConsentView.getFormattedConsentDate()).isEqualTo("11 January 2021");
      assertThat(parallelConsentView.getPwaApplicationId()).isEqualTo(application.getId());
      assertThat(parallelConsentView.getPwaApplicationType()).isEqualTo(application.getApplicationType());
    });
  }

  @Test
  public void getPreSendForApprovalChecksView_initialPWA_noFailedChecks() {

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getFailedSendForApprovalChecks())
        .isEmpty();
    assertThat(preSendApprovalCheckView.getParallelConsentViews())
        .isEmpty();
  }

  @Test
  public void getPreSendForApprovalChecksView_containsNonBlockingWarningView() {

    var consent = new PwaConsent();
    consent.setId(10);
    consent.setReference("consentReference");
    consent.setConsentInstant(APP_CREATION_INSTANT.plus(10, ChronoUnit.DAYS));
    consent.setSourcePwaApplication(detail.getPwaApplication());

    when(pwaConsentService.getPwaConsentsWhereConsentInstantAfter(detail.getMasterPwa(), APP_CREATION_INSTANT))
        .thenReturn(List.of(consent));

    var warningView = AppProcessingTaskWarningTestUtil.createWithWarning(detail.getPwaApplication());
    when(appProcessingTaskWarningService.getNonBlockingTasksWarning(detail.getPwaApplication(), NonBlockingWarningPage.SEND_FOR_APPROVAL))
        .thenReturn(warningView);

    var preSendApprovalCheckView = sendforApprovalCheckerService.getPreSendForApprovalChecksView(detail);

    assertThat(preSendApprovalCheckView.getNonBlockingTasksWarning()).isEqualTo(warningView);
  }


}