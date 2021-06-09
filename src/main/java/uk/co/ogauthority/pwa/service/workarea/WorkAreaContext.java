package uk.co.ogauthority.pwa.service.workarea;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;

/**
 * Store all relevant required to process a workarea tab for a user.
 */
public final class WorkAreaContext {
  private final AuthenticatedUserAccount authenticatedUserAccount;
  private final List<WorkAreaTab> sortedUserTabs;

  // all the different subscriber types suitable for the context user.
  private final Set<ApplicationEventSubscriberType> applicationEventSubscriberTypes;

  WorkAreaContext(AuthenticatedUserAccount authenticatedUserAccount,
                  Set<ApplicationEventSubscriberType> applicationEventSubscriberTypes,
                  List<WorkAreaTab> sortedUserTabs) {
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.applicationEventSubscriberTypes = Collections.unmodifiableSet(applicationEventSubscriberTypes);
    this.sortedUserTabs = Collections.unmodifiableList(sortedUserTabs);
  }

  public Set<ApplicationEventSubscriberType> getApplicationEventSubscriberTypes() {
    return applicationEventSubscriberTypes;
  }

  public PersonId getPersonId() {
    return this.authenticatedUserAccount.getLinkedPerson().getId();
  }

  public int getWuaId() {
    return this.authenticatedUserAccount.getWuaId();
  }

  public AuthenticatedUserAccount getAuthenticatedUserAccount() {
    return authenticatedUserAccount;
  }

  public boolean containsAppEventSubscriberType(ApplicationEventSubscriberType applicationEventSubscriberType) {
    return applicationEventSubscriberTypes.contains(applicationEventSubscriberType);
  }

  public List<WorkAreaTab> getSortedUserTabs() {
    return sortedUserTabs;
  }

  public Optional<WorkAreaTab> getDefaultTab() {
    return !sortedUserTabs.isEmpty()
        ? Optional.of(sortedUserTabs.get(0))
        : Optional.empty();
  }
}
