package uk.co.ogauthority.pwa.model.form.consultation;


import java.time.Instant;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public class ConsultationRequestView {

  private String consulteeGroupName;
  private Instant requestDate;
  private ConsultationRequestStatus status;
  private Instant dueDate;

  public String getConsulteeGroupName() {
    return consulteeGroupName;
  }

  public void setConsulteeGroupName(String consulteeGroupName) {
    this.consulteeGroupName = consulteeGroupName;
  }

  public Instant getRequestDate() {
    return requestDate;
  }

  public void setRequestDate(Instant requestDate) {
    this.requestDate = requestDate;
  }

  public ConsultationRequestStatus getStatus() {
    return status;
  }

  public void setStatus(ConsultationRequestStatus status) {
    this.status = status;
  }

  public Instant getDueDate() {
    return dueDate;
  }

  public void setDueDate(Instant dueDate) {
    this.dueDate = dueDate;
  }
}
