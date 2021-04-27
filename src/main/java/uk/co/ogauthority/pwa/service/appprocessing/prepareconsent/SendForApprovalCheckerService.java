package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;


import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;

@Service
public class SendForApprovalCheckerService {

  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ConsultationRequestService consultationRequestService;
  private final PublicNoticeService publicNoticeService;
  private final DocumentInstanceService documentInstanceService;
  private final MasterPwaService masterPwaService;

  @Autowired
  public SendForApprovalCheckerService(ApplicationUpdateRequestService applicationUpdateRequestService,
                                       ConsultationRequestService consultationRequestService,
                                       PublicNoticeService publicNoticeService,
                                       DocumentInstanceService documentInstanceService,
                                       MasterPwaService masterPwaService) {
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.consultationRequestService = consultationRequestService;
    this.publicNoticeService = publicNoticeService;
    this.documentInstanceService = documentInstanceService;
    this.masterPwaService = masterPwaService;
  }

  public Set<FailedSendForApprovalCheck> getReasonsToPreventSendForApproval(PwaApplicationDetail detail) {

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
