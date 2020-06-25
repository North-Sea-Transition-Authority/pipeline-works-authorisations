package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class AddPipelineHuooJourneyData implements Serializable {
  // Random but consistent uuid for class so de-serialisation of session objects wont crash apps after restarts.
  // If this file changes in a way that means any serialised object would be incompatible, this needs to change.
  // Examples of changes are renaming/adding/removing/changing type of member variables
  // Its likely there would need to be a patch that clears the session variable table after any such change
  // TODO PWA-633
  private static final long serialVersionUID = 1L;
  private HuooRole journeyRoleType;
  private Set<String> pickedPipelineIds = new HashSet<>();
  private Set<Integer> organisationUnitIds = new HashSet<>();

  public AddPipelineHuooJourneyData() {
  }

  public HuooRole getJourneyRoleType() {
    return journeyRoleType;
  }

  public Set<String> getPickedPipelineIds() {
    return pickedPipelineIds;
  }

  public Set<Integer> getOrganisationUnitIds() {
    return organisationUnitIds;
  }

  public void reset() {
    this.journeyRoleType = null;
    this.pickedPipelineIds = new HashSet<>();
    this.organisationUnitIds = new HashSet<>();
  }

  /* On every update of form of journey, ensure that the journey data is going to make sense, else reset it.*/
  private void updateResetCheck(HuooRole huooRole) {
    if (!huooRole.equals(this.getJourneyRoleType())) {
      this.reset();
      this.journeyRoleType = huooRole;
    }
  }

  public void updateJourneyPipelineData(HuooRole huooRole, Set<String> pickedPipelineStrings) {
    updateResetCheck(huooRole);
    this.pickedPipelineIds = SetUtils.emptyIfNull(pickedPipelineStrings);
  }

  public void updateJourneyOrganisationData(HuooRole huooRole, Set<Integer> organisationUnitIds) {
    updateResetCheck(huooRole);
    this.organisationUnitIds = SetUtils.emptyIfNull(organisationUnitIds);
  }

  public void updateFormWithPipelineJourneyData(HuooRole huooRole, PickHuooPipelinesForm pickHuooPipelinesForm) {
    updateResetCheck(huooRole);
    pickHuooPipelinesForm.setPickedPipelineStrings(this.getPickedPipelineIds());
  }

  public void updateFormWithOrganisationRoleJourneyData(HuooRole huooRole,
                                                        PickHuooPipelinesForm pickHuooPipelinesForm) {
    updateResetCheck(huooRole);
    pickHuooPipelinesForm.setOrganisationUnitIds(this.getOrganisationUnitIds());
  }

}
