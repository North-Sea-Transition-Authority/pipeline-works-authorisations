package uk.co.ogauthority.pwa.validators.consultations;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;

public class ConsultationRequestValidationHints {


  private ConsultationRequestService consultationRequestService;
  private ConsulteeGroupDetailService consulteeGroupDetailService;
  private PwaApplication pwaApplication;

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

  public void setConsultationRequestService(
      ConsultationRequestService consultationRequestService) {
    this.consultationRequestService = consultationRequestService;
  }

  public ConsulteeGroupDetailService getConsulteeGroupDetailService() {
    return consulteeGroupDetailService;
  }

  public void setConsulteeGroupDetailService(
      ConsulteeGroupDetailService consulteeGroupDetailService) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }
}
