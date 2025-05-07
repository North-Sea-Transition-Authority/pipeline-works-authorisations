package uk.co.ogauthority.pwa.teams.management.form;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@Service
public class NewConsulteeGroupTeamFormValidator {
  private final TeamManagementService teamManagementService;

  public NewConsulteeGroupTeamFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(NewConsulteeGroupTeamForm form, Errors errors) {
    if (form.getConsulteeGroupId() == null || form.getConsulteeGroupId().isEmpty()) {
      errors.rejectValue("consulteeGroupId", "consulteeGroupId.required", "Select a consultee group");
      return false;
    }

    var teamType = TeamType.CONSULTEE;
    var scopeRef = TeamScopeReference.from(form.getConsulteeGroupId(), teamType);
    if (teamManagementService.doesScopedTeamWithReferenceExist(teamType, scopeRef)) {
      errors.rejectValue("consulteeGroupId", "consulteeGroupId.alreadyExists", "A team for this consultee group already exists");
    }

    return !errors.hasErrors();
  }

}
