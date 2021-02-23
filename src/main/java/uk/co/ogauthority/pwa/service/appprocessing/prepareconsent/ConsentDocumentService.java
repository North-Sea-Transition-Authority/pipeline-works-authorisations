package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ConsentDocumentService {

  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ConsultationRequestService consultationRequestService;
  private final PublicNoticeService publicNoticeService;
  private final DocumentInstanceService documentInstanceService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ConsentDocumentEmailService consentDocumentEmailService;
  private final ConsentReviewService consentReviewService;

  @Autowired
  public ConsentDocumentService(ApplicationUpdateRequestService applicationUpdateRequestService,
                                ConsultationRequestService consultationRequestService,
                                PublicNoticeService publicNoticeService,
                                DocumentInstanceService documentInstanceService,
                                PwaApplicationDetailService pwaApplicationDetailService,
                                ConsentDocumentEmailService consentDocumentEmailService,
                                ConsentReviewService consentReviewService) {
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.consultationRequestService = consultationRequestService;
    this.publicNoticeService = publicNoticeService;
    this.documentInstanceService = documentInstanceService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consentDocumentEmailService = consentDocumentEmailService;
    this.consentReviewService = consentReviewService;
  }

  public boolean canSendForApproval(PwaApplicationDetail detail) {

    boolean latestAppVersionSatisfactory = detail.isTipFlag() && detail.getConfirmedSatisfactoryTimestamp() != null;
    boolean updateInProgress = applicationUpdateRequestService.applicationHasOpenUpdateRequest(detail);
    boolean consultationInProgress = !consultationRequestService
        .getAllOpenRequestsByApplication(detail.getPwaApplication()).isEmpty();
    boolean publicNoticeInProgress = publicNoticeService.publicNoticeInProgress(detail.getPwaApplication());

    boolean documentHasClauses = documentInstanceService
        .getDocumentInstance(detail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT)
        .stream()
        .flatMap(instance -> documentInstanceService.getDocumentView(instance).getSections().stream())
        .anyMatch(section -> section.getClauses().size() >= 1);

    return latestAppVersionSatisfactory && !updateInProgress && !consultationInProgress && !publicNoticeInProgress && documentHasClauses;

  }

  @Transactional
  public void sendForApproval(PwaApplicationDetail pwaApplicationDetail,
                              String coverLetterText,
                              AuthenticatedUserAccount sendingUser) {

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.CONSENT_REVIEW, sendingUser);

    consentReviewService.startConsentReview(pwaApplicationDetail, coverLetterText, sendingUser.getLinkedPerson());

    consentDocumentEmailService.sendConsentReviewStartedEmail(pwaApplicationDetail, sendingUser.getLinkedPerson());

    // todo camunda PWA-1144

  }

}
