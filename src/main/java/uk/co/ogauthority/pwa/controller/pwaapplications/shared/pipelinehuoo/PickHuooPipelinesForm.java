package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import java.util.Set;

public class PickHuooPipelinesForm {

  private Set<String> pickedPipelineStrings;

  private Set<Integer> organisationUnitIds;

  public Set<Integer> getOrganisationUnitIds() {
    return organisationUnitIds;
  }

  public void setOrganisationUnitIds(Set<Integer> organisationUnitIds) {
    this.organisationUnitIds = organisationUnitIds;
  }

  public Set<String> getPickedPipelineStrings() {
    return pickedPipelineStrings;
  }

  public void setPickedPipelineStrings(Set<String> pickedPipelineStrings) {
    this.pickedPipelineStrings = pickedPipelineStrings;
  }

}
