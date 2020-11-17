package uk.co.ogauthority.pwa.model.dto.appprocessing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;

public class ConsultationInvolvementDto {

  private final ConsulteeGroupDetail consulteeGroupDetail;
  private final Set<ConsulteeGroupMemberRole> consulteeRoles;
  private final ConsultationRequest activeRequest;
  private final List<ConsultationRequest> historicalRequests;
  private final boolean assignedToResponderStage;

  public ConsultationInvolvementDto(ConsulteeGroupDetail consulteeGroupDetail,
                                    Set<ConsulteeGroupMemberRole> consulteeRoles,
                                    ConsultationRequest activeRequest,
                                    List<ConsultationRequest> historicalRequests,
                                    boolean assignedToResponderStage) {
    this.consulteeGroupDetail = consulteeGroupDetail;
    this.consulteeRoles = consulteeRoles;
    this.activeRequest = activeRequest;
    this.historicalRequests = historicalRequests;
    this.assignedToResponderStage = assignedToResponderStage;
  }

  public ConsulteeGroupDetail getConsulteeGroupDetail() {
    return consulteeGroupDetail;
  }

  public Set<ConsulteeGroupMemberRole> getConsulteeRoles() {
    return consulteeRoles;
  }

  public ConsultationRequest getActiveRequest() {
    return activeRequest;
  }

  public List<ConsultationRequest> getHistoricalRequests() {
    return historicalRequests;
  }

  public boolean isAssignedToResponderStage() {
    return assignedToResponderStage;
  }

  public Optional<ConsultationRequestDto> getActiveRequestDto() {
    return Optional.ofNullable(activeRequest)
        .map(r -> new ConsultationRequestDto(consulteeGroupDetail.getName(), r));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationInvolvementDto that = (ConsultationInvolvementDto) o;
    return assignedToResponderStage == that.assignedToResponderStage
        && Objects.equals(consulteeGroupDetail, that.consulteeGroupDetail)
        && Objects.equals(consulteeRoles, that.consulteeRoles)
        && Objects.equals(activeRequest, that.activeRequest)
        && Objects.equals(historicalRequests, that.historicalRequests);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consulteeGroupDetail, consulteeRoles, activeRequest, historicalRequests,
        assignedToResponderStage);
  }
}
