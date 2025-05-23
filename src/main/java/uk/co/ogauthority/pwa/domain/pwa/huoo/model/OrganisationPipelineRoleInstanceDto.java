package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

/* Data object encapsulating link between an organisation's PWA role and a pipeline.
 *  Use Cases:
 *  a) building block object for creating application huoo data from consent model
 *  b) building block object for diffing application huoo data from consent model
 * */
public final class OrganisationPipelineRoleInstanceDto {
  private final OrganisationRoleInstanceDto organisationRoleInstanceDto;
  private final PipelineIdentifier pipelineIdentifier;


  public OrganisationPipelineRoleInstanceDto(Integer organisationUnitId,
                                             String manualOrganisationName,
                                             TreatyAgreement treatyAgreement,
                                             HuooRole huooRole,
                                             HuooType huooType,
                                             Integer pipelineId,
                                             String fromLocation,
                                             IdentLocationInclusionMode fromLocationMode,
                                             String toLocation,
                                             IdentLocationInclusionMode toLocationMode,
                                             Integer sectionNumber
                                             ) {
    this.organisationRoleInstanceDto = new OrganisationRoleInstanceDto(
        organisationUnitId,
        manualOrganisationName,
        treatyAgreement,
        huooRole,
        huooType
    );

    if (ObjectUtils.allNotNull(fromLocation, fromLocationMode, toLocation, toLocationMode, sectionNumber)) {
      this.pipelineIdentifier = PipelineSection.from(pipelineId, fromLocation, fromLocationMode, toLocation, toLocationMode, sectionNumber);
    } else if (pipelineId != null) {
      this.pipelineIdentifier = new PipelineId(pipelineId);
    } else {
      pipelineIdentifier = null;
    }
  }

  public OrganisationPipelineRoleInstanceDto(Integer organisationUnitId,
                                             TreatyAgreement treatyAgreement,
                                             HuooRole huooRole,
                                             HuooType huooType,
                                             Integer pipelineId,
                                             String fromLocation,
                                             IdentLocationInclusionMode fromLocationMode,
                                             String toLocation,
                                             IdentLocationInclusionMode toLocationMode,
                                             Integer sectionNumber
  ) {
    this(organisationUnitId,
        null, // manual org name always null
        treatyAgreement,
        huooRole,
        huooType,
        pipelineId,
        fromLocation,
        fromLocationMode,
        toLocation,
        toLocationMode,
        sectionNumber
    );
  }


  public OrganisationPipelineRoleInstanceDto(Integer organisationUnitId,
                                             String manualOrganisationName,
                                             HuooRole huooRole,
                                             Integer pipelineId) {

    this(organisationUnitId,
        manualOrganisationName, // manual org name always null
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId,
        null,
        null,
        null,
        null,
        null
    );
  }


  public static OrganisationPipelineRoleInstanceDto copyWithoutPipeline(OrganisationPipelineRoleInstanceDto orgPipelineRoleInstanceDto) {

    return new OrganisationPipelineRoleInstanceDto(
        orgPipelineRoleInstanceDto.getOrganisationUnitId() != null ? orgPipelineRoleInstanceDto.getOrganisationUnitId().asInt() : null,
        orgPipelineRoleInstanceDto.getOrganisationRoleOwnerDto().getManualOrganisationName(),
        orgPipelineRoleInstanceDto.getOrganisationRoleOwnerDto().getTreatyAgreement(),
        orgPipelineRoleInstanceDto.getHuooRole(),
        orgPipelineRoleInstanceDto.getHuooType(),
        null, null, null, null, null, null);
  }

  public static OrganisationPipelineRoleInstanceDto manualPipelineRoleInstance(PipelineId pipelineId,
                                                                               HuooRole huooRole,
                                                                               String manualName) {
    return new OrganisationPipelineRoleInstanceDto(null,
        manualName,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId.asInt(),
        null, null, null, null, null);
  }

  public OrganisationUnitId getOrganisationUnitId() {
    return this.organisationRoleInstanceDto.getOrganisationUnitId();
  }

  public OrganisationRoleOwnerDto getOrganisationRoleOwnerDto() {
    return this.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto();
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

  public PipelineIdentifier getPipelineIdentifier() {
    return this.pipelineIdentifier;
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
        && Objects.equals(pipelineIdentifier, that.pipelineIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleInstanceDto, pipelineIdentifier);
  }


}
