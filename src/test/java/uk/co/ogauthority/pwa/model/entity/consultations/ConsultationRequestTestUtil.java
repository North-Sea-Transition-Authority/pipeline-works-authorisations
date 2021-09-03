package uk.co.ogauthority.pwa.model.entity.consultations;

import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public class ConsultationRequestTestUtil {

  private ConsultationRequestTestUtil() {
    // no instantiation
  }

  public static ConsultationRequest createWithStatus(PwaApplication pwaApplication, ConsultationRequestStatus status){
    var consultationRequest  = new ConsultationRequest();
    consultationRequest.setPwaApplication(pwaApplication);
    consultationRequest.setStatus(status);
    return consultationRequest;
  }

  public static ConsultationRequest createWithRespondedRequest(PwaApplication pwaApplication, ConsulteeGroup consulteeGroup){
    var consultationRequest  = new ConsultationRequest();
    consultationRequest.setPwaApplication(pwaApplication);
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    return consultationRequest;
  }





}