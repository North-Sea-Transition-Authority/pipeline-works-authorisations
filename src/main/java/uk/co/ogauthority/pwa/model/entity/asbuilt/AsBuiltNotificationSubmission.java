package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import java.time.LocalDate;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
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

  private LocalDate dateLaid;

  private LocalDate datePipelineBroughtIntoUse;

  private String regulatorSubmissionReason;

  private Boolean tipFlag;

  public AsBuiltNotificationSubmission() {
    //hibernate
  }

  public AsBuiltNotificationSubmission(Integer id,
                                       AsBuiltNotificationGroupPipeline asBuiltNotificationGroupPipeline,
                                       PersonId submittedByPersonId, Instant submittedTimestamp,
                                       AsBuiltNotificationStatus asBuiltNotificationStatus, LocalDate dateLaid,
                                       LocalDate datePipelineBroughtIntoUse, String regulatorSubmissionReason, Boolean tipFlag) {
    this.id = id;
    this.asBuiltNotificationGroupPipeline = asBuiltNotificationGroupPipeline;
    this.submittedByPersonId = submittedByPersonId;
    this.submittedTimestamp = submittedTimestamp;
    this.asBuiltNotificationStatus = asBuiltNotificationStatus;
    this.dateLaid = dateLaid;
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

  public LocalDate getDateLaid() {
    return dateLaid;
  }

  public void setDateLaid(LocalDate dateLaid) {
    this.dateLaid = dateLaid;
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