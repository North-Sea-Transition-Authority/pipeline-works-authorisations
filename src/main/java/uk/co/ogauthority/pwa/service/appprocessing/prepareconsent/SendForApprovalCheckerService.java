package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;


import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning.AppProcessingTaskWarningService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class SendForApprovalCheckerService {

  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ConsultationRequestService consultationRequestService;
  private final PublicNoticeService publicNoticeService;
  private final DocumentInstanceService documentInstanceService;
  private final MasterPwaService masterPwaService;
  private final PwaConsentService pwaConsentService;
  private final AppProcessingTaskWarningService appProcessingTaskWarningService;

  @Autowired
  public SendForApprovalCheckerService(ApplicationUpdateRequestService applicationUpdateRequestService,
                                       ConsultationRequestService consultationRequestService,
                                       PublicNoticeService publicNoticeService,
                                       DocumentInstanceService documentInstanceService,
                                       MasterPwaService masterPwaService,
                                       PwaConsentService pwaConsentService,
                                       AppProcessingTaskWarningService appProcessingTaskWarningService) {
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.consultationRequestService = consultationRequestService;
    this.publicNoticeService = publicNoticeService;
    this.documentInstanceService = documentInstanceService;
    this.masterPwaService = masterPwaService;
    this.pwaConsentService = pwaConsentService;
    this.appProcessingTaskWarningService = appProcessingTaskWarningService;
  }

  PreSendForApprovalChecksView getPreSendForApprovalChecksView(PwaApplicationDetail detail) {

    var application = detail.getPwaApplication();

    var failedChecks = getFailedSendForApprovalChecks(detail);

    var parallelConsents = pwaConsentService.getPwaConsentsWhereConsentInstantAfter(
        detail.getMasterPwa(), application.getApplicationCreatedTimestamp()
    );

    var parallelConsentViews = parallelConsents
        .stream()
        .map(pwaConsent -> {
          var sourcePwaApplication = pwaConsent.getSourcePwaApplication();
          return new ParallelConsentView(
              pwaConsent.getId(),
              pwaConsent.getReference(),
              sourcePwaApplication != null ? sourcePwaApplication.getId() : null,
              sourcePwaApplication != null ? sourcePwaApplication.getApplicationType() : null,
              sourcePwaApplication != null ? sourcePwaApplication.getAppReference() : null,
              pwaConsent.getConsentInstant(),
              DateUtils.formatDate(pwaConsent.getConsentInstant())
          );
        })
        .sorted(Comparator.comparing(ParallelConsentView::getConsentInstant))
        .collect(Collectors.toList());

    var nonBlockingTasksWarning = appProcessingTaskWarningService.getNonBlockingTasksWarning(detail.getPwaApplication());

    return new PreSendForApprovalChecksView(
        // basic sort to ensure consistency on from view.
        failedChecks.stream()
            .sorted(Comparator.comparing(FailedSendForApprovalCheck::getReason))
            .collect(Collectors.toUnmodifiableList()),
        parallelConsentViews,
        nonBlockingTasksWarning);

  }

  private Set<FailedSendForApprovalCheck> getFailedSendForApprovalChecks(PwaApplicationDetail detail) {
    var failedChecks = new HashSet<FailedSendForApprovalCheck>();

    if (!(detail.isTipFlag() && detail.getConfirmedSatisfactoryTimestamp() != null)) {
      failedChecks.add(
          new FailedSendForApprovalCheck(SendConsentForApprovalRequirement.LATEST_APP_VERSION_IS_SATISFACTORY));

    }

    if (applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail)) {
      failedChecks.add(new FailedSendForApprovalCheck(SendConsentForApprovalRequirement.NO_UPDATE_IN_PROGRESS));
    }

    if (!consultationRequestService.getAllOpenRequestsByApplication(detail.getPwaApplication()).isEmpty()) {
      failedChecks.add(new FailedSendForApprovalCheck(SendConsentForApprovalRequirement.NO_CONSULTATION_IN_PROGRESS));
    }
    if (publicNoticeService.publicNoticeInProgress(detail.getPwaApplication())) {
      failedChecks.add(new FailedSendForApprovalCheck(SendConsentForApprovalRequirement.NO_PUBLIC_NOTICE_IN_PROGRESS));
    }

    boolean documentHasClauses = documentInstanceService
        .getDocumentInstance(detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)
        .stream()
        .flatMap(instance -> documentInstanceService.getDocumentView(instance).getSections().stream())
        .anyMatch(section -> !section.getClauses().isEmpty());

    if (!documentHasClauses) {
      failedChecks.add(new FailedSendForApprovalCheck(SendConsentForApprovalRequirement.DOCUMENT_HAS_CLAUSES));
    }

    var applicationMasterPwaDetailStatus = masterPwaService.getCurrentDetailOrThrow(
        detail.getMasterPwa()).getMasterPwaDetailStatus();
    if (applicationMasterPwaDetailStatus.equals(MasterPwaDetailStatus.APPLICATION)
        && !detail.getPwaApplicationType().equals(PwaApplicationType.INITIAL)) {
      failedChecks.add(new FailedSendForApprovalCheck(SendConsentForApprovalRequirement.MASTER_PWA_IS_NOT_CONSENTED));
    }

    return failedChecks;

  }

}
