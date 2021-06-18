package uk.co.ogauthority.pwa.model.form.teammanagement;

import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;

public class AddOrganisationTeamForm implements NewTeamForm {

  @NotNull(message = "Select an organisation", groups = {FullValidation.class, PartialValidation.class})
  private String organisationGroup;

  public AddOrganisationTeamForm() {}

  public AddOrganisationTeamForm(String organisationGroup) {
    this.organisationGroup = organisationGroup;
  }

  public String getOrganisationGroup() {
    return organisationGroup;
  }

  public void setOrganisationGroup(String organisationGroup) {
    this.organisationGroup = organisationGroup;
  }
}
