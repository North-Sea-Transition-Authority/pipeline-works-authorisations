package uk.co.ogauthority.pwa.model.form.consultation;


import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

public class ConsultationRequestView {

  private final Integer consultationRequestId;
  private final String consulteeGroupName;
  private final String requestDateDisplay;
  private final ConsultationRequestStatus status;
  private final String dueDateDisplay;
  private final String responseDateDisplay;
  private final ConsultationResponseOption responseType;
  private final String responseRejectionReason;
  private final String responseByPerson;
  private final Boolean canWithdraw;
  private final String withdrawnByUser;
  private final String endTimeStamp;

  public ConsultationRequestView(Integer consultationRequestId,
                                 String consulteeGroupName,
                                 String requestDateDisplay,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay,
                                 Boolean canWithdraw,
                                 String withdrawnByUser,
                                 String endTimeStamp) {
    this.consultationRequestId = consultationRequestId;
    this.consulteeGroupName = consulteeGroupName;
    this.requestDateDisplay = requestDateDisplay;
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDateDisplay = null;
    this.responseType = null;
    this.responseRejectionReason = null;
    this.responseByPerson = null;
    this.canWithdraw = canWithdraw;
    this.withdrawnByUser = withdrawnByUser;
    this.endTimeStamp = endTimeStamp;
  }

  public ConsultationRequestView(Integer consultationRequestId,
                                 String consulteeGroupName,
                                 String requestDateDisplay,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay,
                                 String responseDateDisplay,
                                 ConsultationResponseOption responseType,
                                 Boolean canWithdraw,
                                 String responseByPerson,
                                 String responseRejectionReason) {
    this.consultationRequestId = consultationRequestId;
    this.consulteeGroupName = consulteeGroupName;
    this.requestDateDisplay = requestDateDisplay;
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDateDisplay = responseDateDisplay;
    this.responseType = responseType;
    this.responseRejectionReason = responseRejectionReason;
    this.responseByPerson = responseByPerson;
    this.canWithdraw = canWithdraw;
    this.endTimeStamp = null;
    this.withdrawnByUser = null;
  }

  public Integer getConsultationRequestId() {
    return consultationRequestId;
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

  public Boolean getCanWithdraw() {
    return canWithdraw;
  }

  public String getWithdrawnByUser() {
    return withdrawnByUser;
  }

  public String getEndTimeStamp() {
    return endTimeStamp;
  }
}
