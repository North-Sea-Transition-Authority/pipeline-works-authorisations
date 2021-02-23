package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;

@Service
public class ConsentDocumentService {

  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ConsultationRequestService consultationRequestService;
  private final PublicNoticeService publicNoticeService;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  public ConsentDocumentService(ApplicationUpdateRequestService applicationUpdateRequestService,
                                ConsultationRequestService consultationRequestService,
                                PublicNoticeService publicNoticeService,
                                DocumentInstanceService documentInstanceService) {
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.consultationRequestService = consultationRequestService;
    this.publicNoticeService = publicNoticeService;
    this.documentInstanceService = documentInstanceService;
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

  public void sendForApproval(PwaApplication pwaApplication,
                              String coverLetterText,
                              Person sendingPerson) {
    // TODO PWA-1144 fill this out
  }

}
