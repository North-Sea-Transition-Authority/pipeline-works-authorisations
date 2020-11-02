package uk.co.ogauthority.pwa.model.dto.consultations;

import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;

public class ConsultationRequestDto {

  private final String consulteeGroupName;
  private final ConsultationRequest consultationRequest;

  public ConsultationRequestDto(String consulteeGroupName,
                                ConsultationRequest consultationRequest) {
    this.consulteeGroupName = consulteeGroupName;
    this.consultationRequest = consultationRequest;
  }

  public String getConsulteeGroupName() {
    return consulteeGroupName;
  }

  public ConsultationRequest getConsultationRequest() {
    return consultationRequest;
  }
}
