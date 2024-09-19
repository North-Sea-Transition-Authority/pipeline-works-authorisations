package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;

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
import java.time.Clock;
import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Entity
@Table(name = "application_update_requests")
public class ApplicationUpdateRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pad_id")
  private PwaApplicationDetail pwaApplicationDetail;

  @Basic // this annotation allows the Jpa metamodel to pick up the field, but leaves default behaviour intact.
  // Suitable as PersonId just wraps a basic class.
  @Convert(converter = PersonIdConverter.class)
  private PersonId requestedByPersonId;

  private Instant requestedTimestamp;

  @Column(name = "request_reason", columnDefinition = "CLOB")
  private String requestReason;

  private Instant deadlineTimestamp;

  @Enumerated(EnumType.STRING)
  private ApplicationUpdateRequestStatus status;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId responseByPersonId;

  private Instant responseTimestamp;

  private String responseOtherChanges;

  @ManyToOne
  @JoinColumn(name = "response_pad_id")
  private PwaApplicationDetail responsePwaApplicationDetail;


  public static ApplicationUpdateRequest createRequest(PwaApplicationDetail pwaApplicationDetail,
                                         Person creatorPerson,
                                         Clock clock,
                                         String requestReason,
                                         Instant deadlineTimestamp) {
    var updateRequest = new ApplicationUpdateRequest();
    updateRequest.setPwaApplicationDetail(pwaApplicationDetail);
    updateRequest.setRequestedByPersonId(creatorPerson.getId());
    updateRequest.setRequestReason(requestReason);
    updateRequest.setDeadlineTimestamp(deadlineTimestamp);
    updateRequest.setRequestedTimestamp(clock.instant());
    updateRequest.setStatus(ApplicationUpdateRequestStatus.OPEN);
    return updateRequest;
  }

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

  public PersonId getRequestedByPersonId() {
    return requestedByPersonId;
  }

  public void setRequestedByPersonId(PersonId requestedByPersonId) {
    this.requestedByPersonId = requestedByPersonId;
  }

  public Instant getRequestedTimestamp() {
    return requestedTimestamp;
  }

  public void setRequestedTimestamp(Instant requestedTimestamp) {
    this.requestedTimestamp = requestedTimestamp;
  }

  public String getRequestReason() {
    return requestReason;
  }

  public void setRequestReason(String requestReason) {
    this.requestReason = requestReason;
  }

  public Instant getDeadlineTimestamp() {
    return deadlineTimestamp;
  }

  public void setDeadlineTimestamp(Instant deadlineTimestamp) {
    this.deadlineTimestamp = deadlineTimestamp;
  }

  public ApplicationUpdateRequestStatus getStatus() {
    return status;
  }

  public void setStatus(ApplicationUpdateRequestStatus status) {
    this.status = status;
  }

  public PersonId getResponseByPersonId() {
    return responseByPersonId;
  }

  public void setResponseByPersonId(PersonId responseByPersonId) {
    this.responseByPersonId = responseByPersonId;
  }

  public Instant getResponseTimestamp() {
    return responseTimestamp;
  }

  public void setResponseTimestamp(Instant responseTimestamp) {
    this.responseTimestamp = responseTimestamp;
  }

  public String getResponseOtherChanges() {
    return responseOtherChanges;
  }

  public void setResponseOtherChanges(String responseOtherChanges) {
    this.responseOtherChanges = responseOtherChanges;
  }

  public PwaApplicationDetail getResponsePwaApplicationDetail() {
    return responsePwaApplicationDetail;
  }

  public void setResponsePwaApplicationDetail(
      PwaApplicationDetail responsePwaApplicationDetail) {
    this.responsePwaApplicationDetail = responsePwaApplicationDetail;
  }
}
