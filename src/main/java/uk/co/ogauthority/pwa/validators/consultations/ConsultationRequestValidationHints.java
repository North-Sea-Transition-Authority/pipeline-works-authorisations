package uk.co.ogauthority.pwa.validators.consultations;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;

public class ConsultationRequestValidationHints {


  private final ConsultationRequestService consultationRequestService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final PwaApplication pwaApplication;

  public ConsultationRequestValidationHints(
      ConsultationRequestService consultationRequestService,
      ConsulteeGroupDetailService consulteeGroupDetailService,
      PwaApplication pwaApplication) {
    this.consultationRequestService = consultationRequestService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.pwaApplication = pwaApplication;
  }

  public ConsultationRequestService getConsultationRequestService() {
    return consultationRequestService;
  }

  public ConsulteeGroupDetailService getConsulteeGroupDetailService() {
    return consulteeGroupDetailService;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }
}
