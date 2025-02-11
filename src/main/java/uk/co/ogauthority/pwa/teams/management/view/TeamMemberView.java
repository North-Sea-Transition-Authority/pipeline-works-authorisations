package uk.co.ogauthority.pwa.teams.management.view;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.management.TeamManagementController;

public record TeamMemberView(
    Long wuaId,
    String title,
    String forename,
    String surname,
    String email,
    String telNo,
    UUID teamId,
    List<Role> roles
) {
  public String getDisplayName() {
    return Stream.of(title, forename, surname)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(" "));
  }

  public String getEditUrl() {
    return ReverseRouter.route(on(TeamManagementController.class).renderUserTeamRoles(teamId, wuaId, null));
  }

  public String getRemoveUrl() {
    return ReverseRouter.route(on(TeamManagementController.class).renderRemoveTeamMember(teamId, wuaId));
  }

  public static TeamMemberView fromEpaUser(User user, UUID teamId, List<Role> roles) {
    return new TeamMemberView(
        user.getWebUserAccountId().longValue(),
        user.getTitle(),
        user.getForename(),
        user.getSurname(),
        user.getPrimaryEmailAddress(),
        user.getTelephoneNumber(),
        teamId,
        roles
    );
  }
}