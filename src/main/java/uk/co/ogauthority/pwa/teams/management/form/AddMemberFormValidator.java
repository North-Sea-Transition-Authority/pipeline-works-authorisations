package uk.co.ogauthority.pwa.teams.management.form;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@Service
public class AddMemberFormValidator {

  private static final String FIELD_NAME = "emailAddress";

  private final TeamManagementService teamManagementService;

  public AddMemberFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(AddMemberForm form, UUID teamId, Errors errors) {

    if (StringUtils.isBlank(form.getEmailAddress())) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".required", "Enter a UK Energy Portal email address");
      return false;
    }

    var users = teamManagementService.getEnergyPortalUser(form.getEmailAddress());
    if (users.isEmpty()) {
      errors.rejectValue(FIELD_NAME, FIELD_NAME + ".notFound", "No UK Energy Portal account exists with this email address");
      return false;
    }

    var user = users.get();

    if (!user.getCanLogin()) {
      errors.rejectValue(
          FIELD_NAME,
          FIELD_NAME + ".inactiveAccount",
          "This user does not have login access to the UK Energy Portal and can't be added to this service"
      );
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