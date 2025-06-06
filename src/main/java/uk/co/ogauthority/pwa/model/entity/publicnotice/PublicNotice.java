package uk.co.ogauthority.pwa.model.entity.publicnotice;

import jakarta.persistence.Basic;
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
import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowSubject;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowType;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;

@Entity
@Table(name = "public_notices")
public class PublicNotice implements WorkflowSubject {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_id")
  @ManyToOne
  private PwaApplication pwaApplication;

  @Enumerated(EnumType.STRING)
  private PublicNoticeStatus status;

  private Integer version;

  private Instant withdrawalTimestamp;

  private String withdrawalReason;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId withdrawingPersonId;



  public PublicNotice() {
  }

  public PublicNotice(PwaApplication pwaApplication,
                      PublicNoticeStatus status, Integer version) {
    this.pwaApplication = pwaApplication;
    this.status = status;
    this.version = version;
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

  public PublicNoticeStatus getStatus() {
    return status;
  }

  public void setStatus(PublicNoticeStatus status) {
    this.status = status;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Instant getWithdrawalTimestamp() {
    return withdrawalTimestamp;
  }

  public void setWithdrawalTimestamp(Instant withdrawalTimestamp) {
    this.withdrawalTimestamp = withdrawalTimestamp;
  }

  public String getWithdrawalReason() {
    return withdrawalReason;
  }

  public void setWithdrawalReason(String withdrawalReason) {
    this.withdrawalReason = withdrawalReason;
  }

  public PersonId getWithdrawingPersonId() {
    return withdrawingPersonId;
  }

  public void setWithdrawingPersonId(PersonId withdrawingPersonId) {
    this.withdrawingPersonId = withdrawingPersonId;
  }

  @Override
  public Integer getBusinessKey() {
    return getId();
  }

  @Override
  public WorkflowType getWorkflowType() {
    return WorkflowType.PWA_APPLICATION_PUBLIC_NOTICE;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicNotice that = (PublicNotice) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplication, that.pwaApplication)
        && status == that.status
        && Objects.equals(version, that.version)
        && Objects.equals(withdrawalTimestamp, that.withdrawalTimestamp)
        && Objects.equals(withdrawalReason, that.withdrawalReason)
        && Objects.equals(withdrawingPersonId, that.withdrawingPersonId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplication, status, version, withdrawalTimestamp, withdrawalReason, withdrawingPersonId);
  }
}
