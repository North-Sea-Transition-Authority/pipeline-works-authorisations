package uk.co.ogauthority.pwa.teams.management.form;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@Service
public class AddMemberFormValidator {

  private static final String FIELD_NAME = "username";

  private final TeamManagementService teamManagementService;

  public AddMemberFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(AddMemberForm form, UUID teamId, Errors errors) {

    if (StringUtils.isBlank(form.getUsername())) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".required", "Enter an Energy Portal username");
      return false;
    }

    var users = teamManagementService.getEnergyPortalUser(form.getUsername());
    if (users.isEmpty()) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".notFound", "No Energy Portal user exists with this username");
      return false;
    }

    if (users.size() > 1) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".tooMany",
          "More than one Energy Portal user exists with this email address. Enter the username of the user instead.");
    }

    var user = users.getFirst();
    if (user.getIsAccountShared()) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".sharedAccount", "You cannot add shared accounts to this service");
    }

    if (!user.getCanLogin()) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".inactiveAccount",
          "This user does not have login access to the Energy Portal and can't be added to this service");
    }

    var team = teamManagementService.getTeam(teamId)
        .orElseThrow(() -> new IllegalStateException("Team %s not found".formatted(teamId)));

    if (team.getTeamType().isScoped()
        && !teamManagementService.canAddUserToTeam(user.getWebUserAccountId(), team)
    ) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".alreadyInTeamType",
          "This user is already member of a %s team".formatted(team.getTeamType().getDisplayName()));
    }

    return !errors.hasErrors();
  }
}
