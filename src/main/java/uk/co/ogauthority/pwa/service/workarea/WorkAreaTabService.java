package uk.co.ogauthority.pwa.service.workarea;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@Service
public class WorkAreaTabService {

  private final UserTypeService userTypeService;

  @Autowired
  public WorkAreaTabService(UserTypeService userTypeService) {

    this.userTypeService = userTypeService;
  }

  public Optional<WorkAreaTab> getDefaultTabForUser(AuthenticatedUserAccount user) {

    var availableTabs = getTabsAvailableToUser(user);

    if (availableTabs.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(availableTabs.get(0));

  }

  public List<WorkAreaTab> getTabsAvailableToUser(AuthenticatedUserAccount authenticatedUserAccount) {
    var userType = userTypeService.getUserType(authenticatedUserAccount);

    return WorkAreaTab.stream()
        .filter(tab -> tab.getUserType().equals(userType))
        .sorted(Comparator.comparing(WorkAreaTab::getDisplayOrder))
        .collect(Collectors.toUnmodifiableList());
  }

}
