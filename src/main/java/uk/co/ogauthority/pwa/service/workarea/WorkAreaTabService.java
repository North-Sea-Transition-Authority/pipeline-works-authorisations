package uk.co.ogauthority.pwa.service.workarea;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Service
public class WorkAreaTabService {

  private final UserTypeService userTypeService;
  private final TeamService teamService;

  @Autowired
  public WorkAreaTabService(UserTypeService userTypeService,
                            TeamService teamService) {

    this.userTypeService = userTypeService;
    this.teamService = teamService;
  }

  public Optional<WorkAreaTab> getDefaultTabForUser(AuthenticatedUserAccount user) {

    var availableTabs = getTabsAvailableToUser(user);

    if (availableTabs.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(availableTabs.get(0));
  }

  public List<WorkAreaTab> getTabsAvailableToUser(AuthenticatedUserAccount authenticatedUserAccount) {
    //either get tabs based on user type or based on specific privs
    return WorkAreaTab.stream()
        .filter(tab -> isUserAllowedToAccessTab(authenticatedUserAccount, tab))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .collect(Collectors.toUnmodifiableList());
  }

  private boolean isUserAllowedToAccessTab(AuthenticatedUserAccount userAccount, WorkAreaTab tab) {
    var userType = userTypeService.getPriorityUserType(userAccount);
    return Objects.nonNull(userType) && tab.getUserTypes().contains(userType)
        || allPwaUserPrivsMatch(userAccount, tab.getPwaUserPrivileges());
  }

  private boolean allPwaUserPrivsMatch(AuthenticatedUserAccount authenticatedUserAccount, Set<PwaUserPrivilege> requiredTabPrivs) {
    return !SetUtils.intersection(requiredTabPrivs,
        teamService.getAllUserPrivilegesForPerson(authenticatedUserAccount.getLinkedPerson())).isEmpty();
  }

}
