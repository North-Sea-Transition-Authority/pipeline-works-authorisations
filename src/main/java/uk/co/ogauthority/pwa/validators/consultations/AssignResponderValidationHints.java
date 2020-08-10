package uk.co.ogauthority.pwa.validators.consultations;

import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;

public class AssignResponderValidationHints {


  private final AssignResponderService assignResponderService;
  private final ConsultationRequest consultationRequest;

  public AssignResponderValidationHints(
      AssignResponderService assignResponderService,
      ConsultationRequest consultationRequest) {
    this.assignResponderService = assignResponderService;
    this.consultationRequest = consultationRequest;
  }

  public AssignResponderService getAssignResponderService() {
    return assignResponderService;
  }

  public ConsultationRequest getConsultationRequest() {
    return consultationRequest;
  }
}
