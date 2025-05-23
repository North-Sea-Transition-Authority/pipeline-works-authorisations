package uk.co.ogauthority.pwa.model.entity.consultations;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;

@Table(name = "consultation_response_data")
@Entity
public class ConsultationResponseData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "cons_response_id")
  @ManyToOne
  private ConsultationResponse consultationResponse;

  @Enumerated(EnumType.STRING)
  private ConsultationResponseOptionGroup responseGroup;

  @Enumerated(EnumType.STRING)
  private ConsultationResponseOption responseType;

  private String responseText;

  public ConsultationResponseData() {
  }

  public ConsultationResponseData(ConsultationResponse consultationResponse) {
    this.consultationResponse = consultationResponse;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ConsultationResponseOptionGroup getResponseGroup() {
    return responseGroup;
  }

  public void setResponseGroup(ConsultationResponseOptionGroup responseGroup) {
    this.responseGroup = responseGroup;
  }

  public ConsultationResponse getConsultationResponse() {
    return consultationResponse;
  }

  public void setConsultationResponse(ConsultationResponse consultationResponse) {
    this.consultationResponse = consultationResponse;
  }

  public ConsultationResponseOption getResponseType() {
    return responseType;
  }

  public void setResponseType(ConsultationResponseOption responseType) {
    this.responseType = responseType;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationResponseData that = (ConsultationResponseData) o;
    return Objects.equals(id, that.id)
        && Objects.equals(consultationResponse, that.consultationResponse)
        && responseGroup == that.responseGroup
        && responseType == that.responseType
        && Objects.equals(responseText, that.responseText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, consultationResponse, responseGroup, responseType, responseText);
  }

}
