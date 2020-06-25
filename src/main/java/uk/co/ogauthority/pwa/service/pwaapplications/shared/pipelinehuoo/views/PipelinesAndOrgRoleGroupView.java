package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class PipelinesAndOrgRoleGroupView {

  private final HuooRole huooRole;
  private final Set<PipelineId> pipelineIdSet;
  private final Set<OrganisationUnitId> organisationUnitIdSet;
  private final List<String> pipelineNumbers;
  private final List<String> organisationNames;

  public PipelinesAndOrgRoleGroupView(HuooRole huooRole,
                                      Set<PipelineId> pipelineIdSet,
                                      Set<OrganisationUnitId> organisationUnitIdSet,
                                      List<String> pipelineNumbers,
                                      List<String> organisationNames) {
    this.huooRole = huooRole;
    this.pipelineIdSet = pipelineIdSet;
    this.organisationUnitIdSet = organisationUnitIdSet;
    this.pipelineNumbers = pipelineNumbers;
    this.organisationNames = organisationNames;
  }


  public HuooRole getHuooRole() {
    return huooRole;
  }

  public Set<PipelineId> getPipelineIdSet() {
    return pipelineIdSet;
  }

  public Set<OrganisationUnitId> getOrganisationUnitIdSet() {
    return organisationUnitIdSet;
  }

  public List<String> getPipelineNumbers() {
    return pipelineNumbers;
  }

  public List<String> getOrganisationNames() {
    return organisationNames;
  }
}
