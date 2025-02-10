package uk.co.ogauthority.pwa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.teams.TeamService;

public class UserSessionPrivilegesServiceTest {

  private UserSessionPrivilegesService userSessionPrivilegesService;

  private TeamService teamService;

  private AuthenticatedUserAccount user;

  @Before
  public void setup() {
    teamService = mock(TeamService.class);
    userSessionPrivilegesService = new UserSessionPrivilegesService(teamService);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
        Set.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE, PwaUserPrivilege.PWA_CONSENT_SEARCH)
    );
  }

  @Test
  public void populateUserPrivileges_verifyServiceInteractions() {

    userSessionPrivilegesService.populateUserPrivileges(user);
    verify(teamService).getAllUserPrivilegesForPerson(user.getLinkedPerson());
  }

  @Test
  public void populateUserPrivileges_userPrivilegesUpdated_andOrgMembershipUpdated() {
    var privList = Set.of(PwaUserPrivilege.PWA_MANAGER);
    when(teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson()))
        .thenReturn(privList);

    userSessionPrivilegesService.populateUserPrivileges(user);

    assertThat(user.getUserPrivileges()).containsAll(privList);
  }
}