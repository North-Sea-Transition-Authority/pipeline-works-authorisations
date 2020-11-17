package uk.co.ogauthority.pwa.model.form.consultation;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.util.DateUtils;

public class ConsultationRequestView {

  private final Integer consultationRequestId;
  private final String consulteeGroupName;
  private final Instant requestDate;
  private final String requestDateDisplay;
  private final ConsultationRequestStatus status;
  private final String dueDateDisplay;
  private final Instant responseDate;
  private final String responseDateDisplay;
  private final ConsultationResponseOption responseType;
  private final String responseConfirmReason;
  private final String responseRejectionReason;
  private final String responseByPerson;
  private final Boolean canWithdraw;
  private final String withdrawnByUser;
  private final String endTimeStamp;

  public ConsultationRequestView(Integer consultationRequestId,
                                 String consulteeGroupName,
                                 Instant requestDate,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay,
                                 Boolean canWithdraw,
                                 String withdrawnByUser,
                                 String endTimeStamp) {
    this.consultationRequestId = consultationRequestId;
    this.consulteeGroupName = consulteeGroupName;
    this.requestDate = requestDate;
    this.requestDateDisplay =  DateUtils.formatDateTime(requestDate.truncatedTo(ChronoUnit.SECONDS));
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDate = null;
    this.responseDateDisplay = null;
    this.responseType = null;
    this.responseConfirmReason = null;
    this.responseRejectionReason = null;
    this.responseByPerson = null;
    this.canWithdraw = canWithdraw;
    this.withdrawnByUser = withdrawnByUser;
    this.endTimeStamp = endTimeStamp;
  }

  public ConsultationRequestView(Integer consultationRequestId,
                                 String consulteeGroupName,
                                 Instant requestDate,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay,
                                 Instant responseDate,
                                 ConsultationResponseOption responseType,
                                 Boolean canWithdraw,
                                 String responseByPerson,
                                 String responseConfirmReason,
                                 String responseRejectionReason) {
    this.consultationRequestId = consultationRequestId;
    this.consulteeGroupName = consulteeGroupName;
    this.requestDate = requestDate;
    this.requestDateDisplay =  DateUtils.formatDateTime(requestDate.truncatedTo(ChronoUnit.SECONDS));
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDate = responseDate;
    this.responseDateDisplay =   DateUtils.formatDateTime(responseDate.truncatedTo(ChronoUnit.SECONDS));
    this.responseType = responseType;
    this.responseConfirmReason = responseConfirmReason;
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

  public Instant getRequestDate() {
    return requestDate;
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

  public Instant getResponseDate() {
    return responseDate;
  }

  public String getResponseDateDisplay() {
    return responseDateDisplay;
  }

  public ConsultationResponseOption getResponseType() {
    return responseType;
  }

  public String getResponseConfirmReason() {
    return responseConfirmReason;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationRequestView that = (ConsultationRequestView) o;
    return Objects.equals(consultationRequestId, that.consultationRequestId)
        && Objects.equals(consulteeGroupName, that.consulteeGroupName)
        && Objects.equals(requestDate, that.requestDate)
        && Objects.equals(requestDateDisplay, that.requestDateDisplay)
        && status == that.status
        && Objects.equals(dueDateDisplay, that.dueDateDisplay)
        && Objects.equals(responseDate, that.responseDate)
        && Objects.equals(responseDateDisplay, that.responseDateDisplay)
        && responseType == that.responseType
        && Objects.equals(responseConfirmReason, that.responseConfirmReason)
        && Objects.equals(responseRejectionReason, that.responseRejectionReason)
        && Objects.equals(responseByPerson, that.responseByPerson)
        && Objects.equals(canWithdraw, that.canWithdraw)
        && Objects.equals(withdrawnByUser, that.withdrawnByUser)
        && Objects.equals(endTimeStamp, that.endTimeStamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consultationRequestId, consulteeGroupName, requestDate, requestDateDisplay, status,
        dueDateDisplay, responseDate, responseDateDisplay, responseType, responseConfirmReason, responseRejectionReason,
        responseByPerson, canWithdraw, withdrawnByUser, endTimeStamp);
  }
}
