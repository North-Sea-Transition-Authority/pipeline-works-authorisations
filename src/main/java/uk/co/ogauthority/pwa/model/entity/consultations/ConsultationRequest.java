package uk.co.ogauthority.pwa.model.entity.consultations;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;


@Entity
@Table(name = "consultation_requests")
public class ConsultationRequest implements WorkflowSubject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @ManyToOne
  private PwaApplicationDetail pwaApplicationDetail;


  @JoinColumn(name = "consultee_group_detail_id")
  @OneToOne
  private ConsulteeGroupDetail consulteeGroupDetail;
  private Boolean otherGroupSelected;
  private String otherGroupLogin;

  private Instant deadlineDate;
  private String status;
  private Instant startTimestamp;
  private Integer startedByPersonId;
  private Instant endTimestamp;
  private Integer endedByPersonId;
  private String endedReason;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public ConsulteeGroupDetail getConsulteeGroupDetail() {
    return consulteeGroupDetail;
  }

  public void setConsulteeGroupDetail(
      ConsulteeGroupDetail consulteeGroupDetail) {
    this.consulteeGroupDetail = consulteeGroupDetail;
  }

  public Boolean getOtherGroupSelected() {
    return otherGroupSelected;
  }

  public void setOtherGroupSelected(Boolean otherGroupSelected) {
    this.otherGroupSelected = otherGroupSelected;
  }

  public String getOtherGroupLogin() {
    return otherGroupLogin;
  }

  public void setOtherGroupLogin(String otherGroupLogin) {
    this.otherGroupLogin = otherGroupLogin;
  }

  public Instant getDeadlineDate() {
    return deadlineDate;
  }

  public void setDeadlineDate(Instant deadlineDate) {
    this.deadlineDate = deadlineDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public Integer getStartedByPersonId() {
    return startedByPersonId;
  }

  public void setStartedByPersonId(Integer startedByPersonId) {
    this.startedByPersonId = startedByPersonId;
  }

  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }

  public Integer getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(Integer endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }

  public String getEndedReason() {
    return endedReason;
  }

  public void setEndedReason(String endedReason) {
    this.endedReason = endedReason;
  }


  @Override
  public Integer getBusinessKey() {
    return id;
  }

  @Override
  public WorkflowType getWorkflowType() {
    return WorkflowType.PWA_APPLICATION_CONSULTATION;
  }





  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationRequest that = (ConsultationRequest) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && Objects.equals(consulteeGroupDetail, that.consulteeGroupDetail)
        && Objects.equals(otherGroupSelected, that.otherGroupSelected)
        && Objects.equals(otherGroupLogin, that.otherGroupLogin)
        && Objects.equals(status, that.status)
        && Objects.equals(startedByPersonId, that.startedByPersonId)
        && Objects.equals(endTimestamp, that.endTimestamp)
        && Objects.equals(endedByPersonId, that.endedByPersonId)
        && Objects.equals(endedReason, that.endedReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, consulteeGroupDetail, otherGroupSelected, otherGroupLogin,
        status, startedByPersonId, endTimestamp, endedByPersonId, endedReason);
  }
}
