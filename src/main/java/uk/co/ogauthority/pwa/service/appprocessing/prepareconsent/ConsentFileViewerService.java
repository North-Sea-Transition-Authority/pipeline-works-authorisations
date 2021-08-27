package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.view.consent.ConsentFileView;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;

@Service
public class ConsentFileViewerService {

  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final ConsultationRequestService consultationRequestService;
  private final ConsultationViewService consultationViewService;

  @Autowired
  public ConsentFileViewerService(ConsulteeGroupDetailService consulteeGroupDetailService,
                                  ConsultationRequestService consultationRequestService,
                                  ConsultationViewService consultationViewService) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.consultationRequestService = consultationRequestService;
    this.consultationViewService = consultationViewService;
  }

  public ConsentFileView getConsentFileView(PwaApplication pwaApplication,
                                            PwaConsentApplicationDto consentDto,
                                            ConsultationResponseDocumentType documentType) {
    var consultationRequestView = getLatestConsultationRequestViewForDocumentType(
        pwaApplication, ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION);

    return new ConsentFileView(consentDto, consultationRequestView.orElse(null));
  }

  private Optional<ConsultationRequestView> getLatestConsultationRequestViewForDocumentType(PwaApplication pwaApplication,
                                                                                            ConsultationResponseDocumentType documentType) {
    var latestRequestOptional = getLatestConsultationRequestForResponseDocType(pwaApplication, documentType);
    return latestRequestOptional.map(consultationViewService::getConsultationRequestView);
  }

  private Optional<ConsultationRequest> getLatestConsultationRequestForResponseDocType(PwaApplication pwaApplication,
                                                                                       ConsultationResponseDocumentType
                                                                                           responseDocumentType) {
    var allRespondedRequests = consultationRequestService.getAllRequestsByAppRespondedOnly(pwaApplication);

    var allGroups = allRespondedRequests.stream()
        .map(ConsultationRequest::getConsulteeGroup)
        .collect(Collectors.toSet());
    var allGroupDetailsForResponseDocType = consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(allGroups).stream()
        .filter(groupDetail -> groupDetail.getConsultationResponseDocumentType() == responseDocumentType)
        .collect(Collectors.toList());

    var allRespondedRequestsForDocType = allRespondedRequests.stream()
        .filter(consultationRequest -> allGroupDetailsForResponseDocType.stream()
            .anyMatch(groupDetail -> groupDetail.getConsulteeGroup().getId().equals(consultationRequest.getConsulteeGroup().getId())))
        .collect(Collectors.toList());

    return allRespondedRequestsForDocType.stream()
        .max(Comparator.comparing(ConsultationRequest::getEndTimestamp));
  }

}
