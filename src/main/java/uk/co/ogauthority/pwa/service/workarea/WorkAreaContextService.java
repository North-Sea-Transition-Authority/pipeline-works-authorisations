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
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
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

  public WorkAreaContext createWorkAreaContext(AuthenticatedUserAccount authenticatedUserAccount) {

    var userAppEventSubscriberTypes = EnumSet.noneOf(ApplicationEventSubscriberType.class);
    // if the selected tab is available to the user get it.
    var userTabs = getTabsAvailableToUser(authenticatedUserAccount);

    if (authenticatedUserAccount.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)) {
      userAppEventSubscriberTypes.add(ApplicationEventSubscriberType.PWA_MANAGER);
    }

    if (authenticatedUserAccount.getUserPrivileges().contains(PwaUserPrivilege.PWA_CASE_OFFICER)) {
      userAppEventSubscriberTypes.add(ApplicationEventSubscriberType.CASE_OFFICER);
    }

    var personIsAppContact = pwaContactService.isPersonApplicationContact(authenticatedUserAccount.getLinkedPerson());
    if (personIsAppContact) {
      userAppEventSubscriberTypes.add(ApplicationEventSubscriberType.APPLICATION_CONTACT);
    }

    return new WorkAreaContext(authenticatedUserAccount, userAppEventSubscriberTypes, userTabs);

  }

}
