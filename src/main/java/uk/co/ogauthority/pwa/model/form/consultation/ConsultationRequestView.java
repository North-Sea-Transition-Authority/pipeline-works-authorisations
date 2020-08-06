package uk.co.ogauthority.pwa.model.form.consultation;


import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public class ConsultationRequestView {

  private final String consulteeGroupName;
  private final String requestDateDisplay;
  private final ConsultationRequestStatus status;
  private final String dueDateDisplay;

  public ConsultationRequestView(String consulteeGroupName,
                                 String requestDateDisplay,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay) {
    this.consulteeGroupName = consulteeGroupName;
    this.requestDateDisplay = requestDateDisplay;
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
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
}
