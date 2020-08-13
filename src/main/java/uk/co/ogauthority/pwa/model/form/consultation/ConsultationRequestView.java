package uk.co.ogauthority.pwa.model.form.consultation;


import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public class ConsultationRequestView {

  private final String consulteeGroupName;
  private final String requestDateDisplay;
  private final ConsultationRequestStatus status;
  private final String dueDateDisplay;
  private final String responseDateDisplay;
  private final ConsultationResponseOption responseType;
  private final String responseRejectionReason;
  private final String responseByPerson;

  public ConsultationRequestView(String consulteeGroupName,
                                 String requestDateDisplay,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay) {
    this.consulteeGroupName = consulteeGroupName;
    this.requestDateDisplay = requestDateDisplay;
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDateDisplay = null;
    this.responseType = null;
    this.responseRejectionReason = null;
    this.responseByPerson = null;
  }

  public ConsultationRequestView(String consulteeGroupName,
                                 String requestDateDisplay,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay,
                                 String responseDateDisplay,
                                 ConsultationResponseOption responseType,
                                 String responseByPerson,
                                 String responseRejectionReason) {
    this.consulteeGroupName = consulteeGroupName;
    this.requestDateDisplay = requestDateDisplay;
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDateDisplay = responseDateDisplay;
    this.responseType = responseType;
    this.responseRejectionReason = responseRejectionReason;
    this.responseByPerson = responseByPerson;
  }

  public String getConsulteeGroupName() {
    return consulteeGroupName;
  }

  public String getRequestDateDisplay() {
    return requestDateDisplay;
  }

  public ConsultationRequestStatus getStatus() {
    return status;
  }

  public String getDueDateDisplay() {
    return dueDateDisplay;
  }

  public String getResponseDateDisplay() {
    return responseDateDisplay;
  }

  public ConsultationResponseOption getResponseType() {
    return responseType;
  }

  public String getResponseRejectionReason() {
    return responseRejectionReason;
  }

  public String getResponseByPerson() {
    return responseByPerson;
  }
}
