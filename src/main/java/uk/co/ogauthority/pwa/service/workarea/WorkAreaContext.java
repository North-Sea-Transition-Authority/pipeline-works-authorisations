package uk.co.ogauthority.pwa.service.workarea;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

/**
 * Store all data required to process a work area tab for a user.
 */
public final class WorkAreaContext {
  private final AuthenticatedUserAccount authenticatedUserAccount;
  private final List<WorkAreaTab> sortedUserTabs;

  // all the different subscriber types suitable for the context user.
  private final Set<WorkAreaUserType> workAreaUserTypes;

  WorkAreaContext(AuthenticatedUserAccount authenticatedUserAccount,
                  Set<WorkAreaUserType> workAreaUserTypes,
                  List<WorkAreaTab> sortedUserTabs) {
    this.authenticatedUserAccount = authenticatedUserAccount;
    this.workAreaUserTypes = Collections.unmodifiableSet(workAreaUserTypes);
    this.sortedUserTabs = Collections.unmodifiableList(sortedUserTabs);
  }

  public Set<WorkAreaUserType> getApplicationEventSubscriberTypes() {
    return workAreaUserTypes;
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

  public boolean containsWorkAreaUserType(WorkAreaUserType workAreaUserType) {
    return workAreaUserTypes.contains(workAreaUserType);
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
