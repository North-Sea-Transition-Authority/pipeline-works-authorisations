package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

public class PickHuooPipelinesForm {

  private Set<String> pickedPipelineStrings;

  private Set<Integer> organisationUnitIds;

  public Set<Integer> getOrganisationUnitIds() {
    return SetUtils.emptyIfNull(organisationUnitIds);
  }

  public Set<TreatyAgreement> treatyAgreements;

  public void setOrganisationUnitIds(Set<Integer> organisationUnitIds) {
    this.organisationUnitIds = organisationUnitIds;
  }

  public Set<String> getPickedPipelineStrings() {
    return SetUtils.emptyIfNull(pickedPipelineStrings);
  }

  public void setPickedPipelineStrings(Set<String> pickedPipelineStrings) {
    this.pickedPipelineStrings = pickedPipelineStrings;
  }

  public Set<TreatyAgreement> getTreatyAgreements() {
    return SetUtils.emptyIfNull(treatyAgreements);
  }

  public void setTreatyAgreements(Set<TreatyAgreement> treatyAgreements) {
    this.treatyAgreements = treatyAgreements;
  }
}
