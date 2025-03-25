package uk.co.ogauthority.pwa.teams.management.form;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@Service
public class NewOrganisationTeamFormValidator {
  private final TeamManagementService teamManagementService;

  public NewOrganisationTeamFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(NewOrganisationTeamForm form, Errors errors) {
    if (form.getOrgGroupId() == null || form.getOrgGroupId().isEmpty()) {
      errors.rejectValue("orgGroupId", "orgGroupId.required", "Select an organisation");
      return false;
    }

    var teamType = TeamType.ORGANISATION;
    var scopeRef = TeamScopeReference.from(form.getOrgGroupId(), teamType);
    if (teamManagementService.doesScopedTeamWithReferenceExist(teamType, scopeRef)) {
      errors.rejectValue("orgGroupId", "orgGroupId.alreadyExists", "A team for this organisation already exists");
    }

    return !errors.hasErrors();
  }

}
