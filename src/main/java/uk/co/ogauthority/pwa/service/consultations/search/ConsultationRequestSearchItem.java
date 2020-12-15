package uk.co.ogauthority.pwa.service.consultations.search;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

@Entity
@Table(name = "consultation_search_items")
@Immutable
public class ConsultationRequestSearchItem {

  @Id
  private Integer consultationRequestId;

  @ManyToOne
  @JoinColumn(name = "tip_pad_id")
  private ApplicationDetailView applicationDetailView;

  private Integer consulteeGroupId;

  private String consulteeGroupName;

  private String consulteeGroupAbbr;

  @Column(name = "request_deadline_date")
  private Instant deadlineDate;

  @Enumerated(EnumType.STRING)
  private ConsultationRequestStatus consultationRequestStatus;

  private String assignedResponderName;

  public ConsultationRequestSearchItem() {
  }

  public ApplicationDetailView getApplicationDetailView() {
    return applicationDetailView;
  }

  public Integer getConsulteeGroupId() {
    return consulteeGroupId;
  }

  public String getConsulteeGroupName() {
    return consulteeGroupName;
  }

  public String getConsulteeGroupAbbr() {
    return consulteeGroupAbbr;
  }

  public Integer getConsultationRequestId() {
    return consultationRequestId;
  }

  public Instant getDeadlineDate() {
    return deadlineDate;
  }

  public ConsultationRequestStatus getConsultationRequestStatus() {
    return consultationRequestStatus;
  }

  public String getAssignedResponderName() {
    return assignedResponderName;
  }

  public void setApplicationDetailView(ApplicationDetailView applicationDetailView) {
    this.applicationDetailView = applicationDetailView;
  }

  public void setConsultationRequestId(Integer consultationRequestId) {
    this.consultationRequestId = consultationRequestId;
  }

  public void setConsulteeGroupId(Integer consulteeGroupId) {
    this.consulteeGroupId = consulteeGroupId;
  }

  public void setConsulteeGroupName(String consulteeGroupName) {
    this.consulteeGroupName = consulteeGroupName;
  }

  public void setConsulteeGroupAbbr(String consulteeGroupAbbr) {
    this.consulteeGroupAbbr = consulteeGroupAbbr;
  }

  public void setDeadlineDate(Instant consultationRequestDeadlineDate) {
    this.deadlineDate = consultationRequestDeadlineDate;
  }

  public void setConsultationRequestStatus(
      ConsultationRequestStatus consultationRequestStatus) {
    this.consultationRequestStatus = consultationRequestStatus;
  }

  public void setAssignedResponderName(String assignedResponderName) {
    this.assignedResponderName = assignedResponderName;
  }

}
