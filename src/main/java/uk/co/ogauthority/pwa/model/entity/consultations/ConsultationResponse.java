package uk.co.ogauthority.pwa.model.entity.consultations;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name = "consultation_responses")
public class ConsultationResponse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "cr_id")
  @OneToOne
  private ConsultationRequest consultationRequest;

  private Instant responseTimestamp;

  @Column(name = "responding_person")
  private Integer respondingPersonId;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ConsultationRequest getConsultationRequest() {
    return consultationRequest;
  }

  public void setConsultationRequest(ConsultationRequest consultationRequest) {
    this.consultationRequest = consultationRequest;
  }

  public Instant getResponseTimestamp() {
    return responseTimestamp;
  }

  public void setResponseTimestamp(Instant responseTimestamp) {
    this.responseTimestamp = responseTimestamp;
  }

  public Integer getRespondingPersonId() {
    return respondingPersonId;
  }

  public void setRespondingPersonId(Integer respondingPersonId) {
    this.respondingPersonId = respondingPersonId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationResponse that = (ConsultationResponse) o;
    return Objects.equals(id, that.id)
        && Objects.equals(consultationRequest, that.consultationRequest)
        && Objects.equals(responseTimestamp, that.responseTimestamp)
        && Objects.equals(respondingPersonId, that.respondingPersonId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, consultationRequest, responseTimestamp, respondingPersonId);
  }
}
