package uk.co.ogauthority.pwa.service.workarea;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class WorkAreaContextService {

  private final UserTypeService userTypeService;
  private final PwaContactService pwaContactService;
  private final HasTeamRoleService hasTeamRoleService;

  @Autowired
  public WorkAreaContextService(UserTypeService userTypeService,
                                PwaContactService pwaContactService,
                                HasTeamRoleService hasTeamRoleService) {

    this.userTypeService = userTypeService;
    this.pwaContactService = pwaContactService;
    this.hasTeamRoleService = hasTeamRoleService;
  }

  @VisibleForTesting
  List<WorkAreaTab> getTabsAvailableToUser(AuthenticatedUserAccount authenticatedUserAccount) {
    var userType = getUserTypePrioritisingConsultee(authenticatedUserAccount);

    return WorkAreaTab.stream()
        .filter(tab -> hasUserTypeInAllowedUserTypes(tab, userType) || hasRoleInAllowedRoleGroup(authenticatedUserAccount, tab))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .toList();
  }

  private UserType getUserTypePrioritisingConsultee(AuthenticatedUserAccount authenticatedUserAccount) {
    return userTypeService.getUserTypes(authenticatedUserAccount)
        .stream()
        .filter(type -> type.equals(UserType.CONSULTEE))
        .findFirst()
        .orElseGet(() -> userTypeService.getPriorityUserType(authenticatedUserAccount).orElse(null));
  }

  private boolean hasRoleInAllowedRoleGroup(AuthenticatedUserAccount userAccount, WorkAreaTab tab) {
    return tab.getRoleGroup()
        .filter(roleGroup -> hasTeamRoleService.userHasAnyRoleInTeamTypes(userAccount, roleGroup.getRolesByTeamType()))
        .isPresent();
  }

  private boolean hasUserTypeInAllowedUserTypes(WorkAreaTab tab, UserType userType) {
    return Objects.nonNull(userType) && tab.getUserTypes().contains(userType);
  }

  public WorkAreaContext createWorkAreaContext(AuthenticatedUserAccount authenticatedUserAccount) {

    var userAppEventSubscriberTypes = EnumSet.noneOf(WorkAreaUserType.class);

    // if the selected tab is available to the user get it.
    var userTabs = getTabsAvailableToUser(authenticatedUserAccount);

    if (hasTeamRoleService.userHasAnyRoleInTeamType(authenticatedUserAccount, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER))) {
      userAppEventSubscriberTypes.add(WorkAreaUserType.PWA_MANAGER);
    }

    if (hasTeamRoleService.userHasAnyRoleInTeamType(authenticatedUserAccount, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER))) {
      userAppEventSubscriberTypes.add(WorkAreaUserType.CASE_OFFICER);
    }

    var personIsAppContact = pwaContactService.isPersonApplicationContact(authenticatedUserAccount.getLinkedPerson());
    if (personIsAppContact) {
      userAppEventSubscriberTypes.add(WorkAreaUserType.APPLICATION_CONTACT);
    }

    return new WorkAreaContext(authenticatedUserAccount, userAppEventSubscriberTypes, userTabs);

  }

}
