package uk.co.ogauthority.pwa.service.consultations;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.consultations.ConsultationController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class ConsultationsUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  public ConsultationsUrlFactory(PwaApplicationType applicationType, Integer applicationId) {
    this.applicationType = applicationType;
    this.applicationId = applicationId;
  }

  public String getWithdrawConsultationUrl(Integer consultationRequestId) {
    return ReverseRouter.route(on(ConsultationController.class)
        .renderWithdrawConsultation(applicationId, applicationType, consultationRequestId,null, null, null));
  }


}
