package uk.co.ogauthority.pwa.model.entity.consultations;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;


@Entity
@Table(name = "consultation_requests")
public class ConsultationRequest implements WorkflowSubject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_id")
  @ManyToOne
  private PwaApplication pwaApplication;

  @JoinColumn(name = "consultee_group_id")
  @OneToOne
  private ConsulteeGroup consulteeGroup;

  private Instant deadlineDate;
  @Enumerated(EnumType.STRING)
  private ConsultationRequestStatus status;
  private Instant startTimestamp;
  private Integer startedByPersonId;
  private Instant endTimestamp;
  private Integer endedByPersonId;
  private String endedReason;

  public ConsultationRequest() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }

  public ConsulteeGroup getConsulteeGroup() {
    return consulteeGroup;
  }

  public void setConsulteeGroup(
      ConsulteeGroup consulteeGroup) {
    this.consulteeGroup = consulteeGroup;
  }

  public Instant getDeadlineDate() {
    return deadlineDate;
  }

  public void setDeadlineDate(Instant deadlineDate) {
    this.deadlineDate = deadlineDate;
  }

  public ConsultationRequestStatus getStatus() {
    return status;
  }

  public void setStatus(ConsultationRequestStatus status) {
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
    return getId();
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
        && Objects.equals(pwaApplication, that.pwaApplication)
        && Objects.equals(consulteeGroup, that.consulteeGroup)
        && Objects.equals(deadlineDate, that.deadlineDate)
        && status == that.status
        && Objects.equals(startTimestamp, that.startTimestamp)
        && Objects.equals(startedByPersonId, that.startedByPersonId)
        && Objects.equals(endTimestamp, that.endTimestamp)
        && Objects.equals(endedByPersonId, that.endedByPersonId)
        && Objects.equals(endedReason, that.endedReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplication, consulteeGroup, deadlineDate, status, startTimestamp, startedByPersonId,
        endTimestamp, endedByPersonId, endedReason);
  }
}
