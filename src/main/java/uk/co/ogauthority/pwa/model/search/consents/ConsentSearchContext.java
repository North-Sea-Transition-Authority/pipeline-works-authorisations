package uk.co.ogauthority.pwa.model.search.consents;

import java.util.Set;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

public class ConsentSearchContext {

  private final AuthenticatedUserAccount user;

  private final UserType userType;

  private Set<Integer> orgGroupIdsUserInTeamFor = Set.of();

  public ConsentSearchContext(AuthenticatedUserAccount user,
                              UserType userType) {
    this.user = user;
    this.userType = userType;
  }

  public AuthenticatedUserAccount getUser() {
    return user;
  }

  public UserType getUserType() {
    return userType;
  }

  public Set<Integer> getOrgGroupIdsUserInTeamFor() {
    return orgGroupIdsUserInTeamFor;
  }

  public void setOrgGroupIdsUserInTeamFor(Set<Integer> orgGroupIdsUserInTeamFor) {
    this.orgGroupIdsUserInTeamFor = orgGroupIdsUserInTeamFor;
  }

}
