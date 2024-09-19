package uk.co.ogauthority.pwa.model.entity.asbuilt;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;

@Entity
@Table(name = "as_built_notif_submissions")
public class AsBuiltNotificationSubmission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "asBuiltNotifPipelineId")
  private AsBuiltNotificationGroupPipeline  asBuiltNotificationGroupPipeline;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId submittedByPersonId;

  private Instant submittedTimestamp;

  @Column(name = "as_built_status")
  @Enumerated(EnumType.STRING)
  private AsBuiltNotificationStatus asBuiltNotificationStatus;

  private LocalDate dateWorkCompleted;

  private LocalDate datePipelineBroughtIntoUse;

  private String regulatorSubmissionReason;

  private Boolean tipFlag;

  public AsBuiltNotificationSubmission() {
    //hibernate
  }

  public AsBuiltNotificationSubmission(Integer id,
                                       AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline,
                                       PersonId submittedByPersonId, Instant submittedTimestamp,
                                       AsBuiltNotificationStatus asBuiltNotificationStatus, LocalDate dateWorkCompleted,
                                       LocalDate datePipelineBroughtIntoUse, String regulatorSubmissionReason, Boolean tipFlag) {
    this.id = id;
    this.asBuiltNotificationGroupPipeline = asBuiltNotificationGroupPipeline;
    this.submittedByPersonId = submittedByPersonId;
    this.submittedTimestamp = submittedTimestamp;
    this.asBuiltNotificationStatus = asBuiltNotificationStatus;
    this.dateWorkCompleted = dateWorkCompleted;
    this.datePipelineBroughtIntoUse = datePipelineBroughtIntoUse;
    this.regulatorSubmissionReason = regulatorSubmissionReason;
    this.tipFlag = tipFlag;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AsBuiltNotificationGroupPipeline getAsBuiltNotificationGroupPipeline() {
    return asBuiltNotificationGroupPipeline;
  }

  public void setAsBuiltNotificationGroupPipeline(
      AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline) {
    this.asBuiltNotificationGroupPipeline = asBuiltNotificationGroupPipeline;
  }

  public PersonId getSubmittedByPersonId() {
    return submittedByPersonId;
  }

  public void setSubmittedByPersonId(PersonId submittedByPersonId) {
    this.submittedByPersonId = submittedByPersonId;
  }

  public Instant getSubmittedTimestamp() {
    return submittedTimestamp;
  }

  public void setSubmittedTimestamp(Instant submittedTimestamp) {
    this.submittedTimestamp = submittedTimestamp;
  }

  public AsBuiltNotificationStatus getAsBuiltNotificationStatus() {
    return asBuiltNotificationStatus;
  }

  public void setAsBuiltNotificationStatus(AsBuiltNotificationStatus asBuiltNotificationStatus) {
    this.asBuiltNotificationStatus = asBuiltNotificationStatus;
  }

  public LocalDate getDateWorkCompleted() {
    return dateWorkCompleted;
  }

  public void setDateWorkCompleted(LocalDate dateWorkCompleted) {
    this.dateWorkCompleted = dateWorkCompleted;
  }

  public LocalDate getDatePipelineBroughtIntoUse() {
    return datePipelineBroughtIntoUse;
  }

  public void setDatePipelineBroughtIntoUse(LocalDate datePipelineBroughtIntoUse) {
    this.datePipelineBroughtIntoUse = datePipelineBroughtIntoUse;
  }

  public String getRegulatorSubmissionReason() {
    return regulatorSubmissionReason;
  }

  public void setRegulatorSubmissionReason(String regulatorSubmissionReason) {
    this.regulatorSubmissionReason = regulatorSubmissionReason;
  }

  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

}