package uk.co.ogauthority.pwa.service.workarea;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Service
public class WorkAreaContextService {

  private final UserTypeService userTypeService;
  private final TeamService teamService;
  private final PwaContactService pwaContactService;

  @Autowired
  public WorkAreaContextService(UserTypeService userTypeService,
                                TeamService teamService,
                                PwaContactService pwaContactService) {

    this.userTypeService = userTypeService;
    this.teamService = teamService;
    this.pwaContactService = pwaContactService;
  }

  @VisibleForTesting
  List<WorkAreaTab> getTabsAvailableToUser(AuthenticatedUserAccount authenticatedUserAccount) {
    //either get tabs based on user type or based on specific privs
    var privs = teamService.getAllUserPrivilegesForPerson(authenticatedUserAccount.getLinkedPerson());

    return WorkAreaTab.stream()
        .filter(tab -> isUserAllowedToAccessTab(authenticatedUserAccount, tab, privs))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .toList();
  }

  private boolean isUserAllowedToAccessTab(AuthenticatedUserAccount userAccount, WorkAreaTab tab,
                                           Set<PwaUserPrivilege> pwaUserPrivileges) {

    var userType = userTypeService.getUserTypes(userAccount)
        .stream()
        .filter(type -> type.equals(UserType.CONSULTEE))
        .findFirst()
        .orElse(userTypeService.getPriorityUserTypeOrThrow(userAccount));

    var allPrivsMatch = !SetUtils.intersection(tab.getPwaUserPrivileges(), pwaUserPrivileges).isEmpty();

    return Objects.nonNull(userType) && tab.getUserTypes().contains(userType) || allPrivsMatch;

  }

  public WorkAreaContext createWorkAreaContext(AuthenticatedUserAccount authenticatedUserAccount) {

    var userAppEventSubscriberTypes = EnumSet.noneOf(WorkAreaUserType.class);

    // if the selected tab is available to the user get it.
    var userTabs = getTabsAvailableToUser(authenticatedUserAccount);

    if (authenticatedUserAccount.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)) {
      userAppEventSubscriberTypes.add(WorkAreaUserType.PWA_MANAGER);
    }

    if (authenticatedUserAccount.getUserPrivileges().contains(PwaUserPrivilege.PWA_CASE_OFFICER)) {
      userAppEventSubscriberTypes.add(WorkAreaUserType.CASE_OFFICER);
    }

    var personIsAppContact = pwaContactService.isPersonApplicationContact(authenticatedUserAccount.getLinkedPerson());
    if (personIsAppContact) {
      userAppEventSubscriberTypes.add(WorkAreaUserType.APPLICATION_CONTACT);
    }

    return new WorkAreaContext(authenticatedUserAccount, userAppEventSubscriberTypes, userTabs);

  }

}
