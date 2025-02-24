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

    var scopeRef = TeamScopeReference.from(form.getOrgGroupId(), "ORGGRP");
    if (teamManagementService.doesScopedTeamWithReferenceExist(TeamType.ORGANISATION, scopeRef)) {
      errors.rejectValue("orgGroupId", "orgGroupId.alreadyExists", "A team for this organisation already exists");
    }

    return !errors.hasErrors();
  }

}
