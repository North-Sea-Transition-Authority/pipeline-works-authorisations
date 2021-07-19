package uk.co.ogauthority.pwa.model.dto.consultations;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;

public class ConsultationRequestDto {

  private final ConsulteeGroupDetail consulteeGroupDetail;
  private final ConsultationRequest consultationRequest;

  public ConsultationRequestDto(ConsulteeGroupDetail consulteeGroupDetail,
                                ConsultationRequest consultationRequest) {
    this.consulteeGroupDetail = consulteeGroupDetail;
    this.consultationRequest = consultationRequest;
  }

  public String getConsulteeGroupName() {
    return consulteeGroupDetail.getName();
  }

  public Set<ConsultationResponseOptionGroup> getConsultationResponseOptionGroups() {
    return consulteeGroupDetail.getResponseOptionGroups();
  }

  public ConsultationRequest getConsultationRequest() {
    return consultationRequest;
  }

}
