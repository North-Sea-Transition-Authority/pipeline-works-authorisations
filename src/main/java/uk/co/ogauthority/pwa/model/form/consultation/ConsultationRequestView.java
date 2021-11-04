package uk.co.ogauthority.pwa.model.form.consultation;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseDataView;
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
  private final List<ConsultationResponseDataView> dataList;
  private final String responseByPerson;
  private final Boolean canWithdraw;
  private final String withdrawnByUser;
  private final String endTimeStamp;
  private final List<UploadedFileView> consultationResponseFileViews;
  private final String downloadFileUrl;
  private final ConsultationResponseDocumentType consultationResponseDocumentType;

  public ConsultationRequestView(Integer consultationRequestId,
                                 String consulteeGroupName,
                                 Instant requestDate,
                                 ConsultationRequestStatus status,
                                 String dueDateDisplay,
                                 List<ConsultationResponseDataView> dataList,
                                 Boolean canWithdraw,
                                 String withdrawnByUser,
                                 String endTimeStamp,
                                 ConsultationResponseDocumentType consultationResponseDocumentType) {
    this.consultationRequestId = consultationRequestId;
    this.consulteeGroupName = consulteeGroupName;
    this.requestDate = requestDate;
    this.requestDateDisplay =  DateUtils.formatDateTime(requestDate.truncatedTo(ChronoUnit.SECONDS));
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.consultationResponseDocumentType = consultationResponseDocumentType;
    this.consultationResponseFileViews = null;
    this.responseDate = null;
    this.downloadFileUrl = null;
    this.responseDateDisplay = null;
    this.responseByPerson = null;
    this.dataList = dataList;
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
                                 List<ConsultationResponseDataView> dataList,
                                 Boolean canWithdraw,
                                 String responseByPerson,
                                 List<UploadedFileView> consultationResponseFileViews,
                                 String downloadFileUrl,
                                 ConsultationResponseDocumentType consultationResponseDocumentType) {
    this.consultationRequestId = consultationRequestId;
    this.consulteeGroupName = consulteeGroupName;
    this.requestDate = requestDate;
    this.requestDateDisplay =  DateUtils.formatDateTime(requestDate.truncatedTo(ChronoUnit.SECONDS));
    this.status = status;
    this.dueDateDisplay = dueDateDisplay;
    this.responseDate = responseDate;
    this.responseDateDisplay = DateUtils.formatDateTime(responseDate.truncatedTo(ChronoUnit.SECONDS));
    this.dataList = dataList;
    this.responseByPerson = responseByPerson;
    this.canWithdraw = canWithdraw;
    this.consultationResponseFileViews = consultationResponseFileViews;
    this.downloadFileUrl = downloadFileUrl;
    this.consultationResponseDocumentType = consultationResponseDocumentType;
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

  public List<ConsultationResponseDataView> getDataList() {
    return dataList;
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

  public List<UploadedFileView> getConsultationResponseFileViews() {
    return consultationResponseFileViews;
  }

  public String getDownloadFileUrl() {
    return downloadFileUrl;
  }

  public ConsultationResponseDocumentType getConsultationResponseDocumentType() {
    return consultationResponseDocumentType;
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
        && Objects.equals(dataList, that.dataList)
        && Objects.equals(responseByPerson, that.responseByPerson)
        && Objects.equals(canWithdraw, that.canWithdraw)
        && Objects.equals(withdrawnByUser, that.withdrawnByUser)
        && Objects.equals(endTimeStamp, that.endTimeStamp)
        && Objects.equals(consultationResponseFileViews, that.consultationResponseFileViews)
        && Objects.equals(downloadFileUrl, that.downloadFileUrl)
        && Objects.equals(consultationResponseDocumentType, that.consultationResponseDocumentType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consultationRequestId, consulteeGroupName, requestDate, requestDateDisplay, status,
        dueDateDisplay, responseDate, responseDateDisplay, dataList, responseByPerson, canWithdraw, withdrawnByUser, endTimeStamp,
        consultationResponseFileViews, downloadFileUrl, consultationResponseDocumentType);
  }

}
