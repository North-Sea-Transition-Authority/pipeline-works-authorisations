package uk.co.ogauthority.pwa.model.dto.consents;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

/* Data object encapsulating link between an organisation's PWA role and a pipeline.
 *  Use Cases:
 *  a) building block object for creating application huoo data from consent model
 *  b) building block object for diffing application huoo data from consent model
 * */
public final class OrganisationPipelineRoleInstanceDto {
  private final OrganisationRoleInstanceDto organisationRoleInstanceDto;
  private final PipelineId pipelineId;


  public OrganisationPipelineRoleInstanceDto(Integer organisationUnitId,
                                             String manualOrganisationName,
                                             TreatyAgreement treatyAgreement,
                                             HuooRole huooRole,
                                             HuooType huooType,
                                             int pipelineId) {
    this.organisationRoleInstanceDto = new OrganisationRoleInstanceDto(
        organisationUnitId,
        manualOrganisationName,
        treatyAgreement,
        huooRole,
        huooType
    );
    this.pipelineId = new PipelineId(pipelineId);
  }

  public OrganisationUnitId getOrganisationUnitId() {
    return this.organisationRoleInstanceDto.getOrganisationUnitId();
  }

  public boolean hasValidOrganisationRole() {
    return this.organisationRoleInstanceDto.isPortalOrgRole();
  }

  public HuooRole getHuooRole() {
    return this.organisationRoleInstanceDto.getHuooRole();
  }

  public HuooType getHuooType() {
    return this.organisationRoleInstanceDto.getHuooType();
  }

  public PipelineId getPipelineId() {
    return this.pipelineId;
  }

  public OrganisationRoleInstanceDto getOrganisationRoleInstanceDto() {
    return this.organisationRoleInstanceDto;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationPipelineRoleInstanceDto that = (OrganisationPipelineRoleInstanceDto) o;
    return Objects.equals(organisationRoleInstanceDto, that.organisationRoleInstanceDto)
        && Objects.equals(pipelineId, that.pipelineId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleInstanceDto, pipelineId);
  }


}
