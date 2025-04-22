package uk.co.ogauthority.pwa.service.users;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class UserTypeService {

  private final TeamQueryService teamQueryService;
  private final PwaContactService pwaContactService;

  public UserTypeService(TeamQueryService teamQueryService, PwaContactService pwaContactService) {
    this.teamQueryService = teamQueryService;
    this.pwaContactService = pwaContactService;
  }

  public UserType getPriorityUserTypeOrThrow(AuthenticatedUserAccount authenticatedUserAccount) {
    return getPriorityUserType(authenticatedUserAccount)
        .orElseThrow(() -> new IllegalStateException(
            "User with WUA ID: %d doesn't match a recognised user type.".formatted(authenticatedUserAccount.getWuaId())
        ));
  }

  public Optional<UserType> getPriorityUserType(AuthenticatedUserAccount authenticatedUserAccount) {
    Set<UserType> userTypes = getUserTypes(authenticatedUserAccount);

    return findPriorityUserTypeFrom(userTypes);
  }

  private Optional<UserType> findPriorityUserTypeFrom(Collection<UserType> userTypeCollection) {
    return userTypeCollection
        .stream()
        .max(Comparator.comparing(UserType::getPriority));

  }

  public Set<UserType> getUserTypes(AuthenticatedUserAccount authenticatedUserAccount) {
    Set<TeamType> teamTypes = teamQueryService.getTeamsUserIsMemberOf(authenticatedUserAccount.getWuaId())
        .stream()
        .map(Team::getTeamType)
        .collect(Collectors.toSet());

    var userTypes = UserType.stream()
        .filter(userType -> teamTypes.contains(userType.getTeamType()))
        .collect(Collectors.toSet());

    if (pwaContactService.isPersonApplicationContact(authenticatedUserAccount.getLinkedPerson())) {
      userTypes.add(UserType.INDUSTRY);
    }

    return userTypes;
  }

}
