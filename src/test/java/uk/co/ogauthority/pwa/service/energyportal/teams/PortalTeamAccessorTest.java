package uk.co.ogauthority.pwa.service.energyportal.teams;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.repo.PortalTeamRepository;

/**
 * Majority of service tests should be in the integration test {@link uk.co.ogauthority.pwa.integration.energyportal.teams.PortalTeamAccessorIntegrationTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class PortalTeamAccessorTest {

  private final int TEAM_RES_ID = 12345;

  @Mock
  private PortalTeamRepository portalTeamRepository;

  @Mock
  private EntityManager entityManager;

  private Person targetPerson;
  private WebUserAccount actionPerformedBy;
  private AuthenticatedUserAccount user;

  private PortalTeamAccessor portalTeamAccessor;


  @Before
  public void setup() {
    portalTeamAccessor = new PortalTeamAccessor(portalTeamRepository, entityManager);

    targetPerson = new Person(1, "fname", "sname", "email", "0");
    actionPerformedBy = new WebUserAccount(9);
    user = new AuthenticatedUserAccount(actionPerformedBy, List.of());
  }


  @Test
  public void removePersonFromTeam_verifyRepositoryInteraction() {
    portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, targetPerson, actionPerformedBy);
    verify(portalTeamRepository, times(1)).removeUserFromTeam(
        TEAM_RES_ID,
        targetPerson.getId().asInt(),
        actionPerformedBy.getWuaId()
    );
  }


  @Test(expected = RuntimeException.class)
  public void removePersonFromTeam_caughtErrorsAreRethrown() {
    doThrow(new NullPointerException()).when(portalTeamRepository).removeUserFromTeam(any(), any(), any());
    portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, targetPerson, actionPerformedBy);
  }

  @Test
  public void addPersonToTeamWithRoles_verifyRepositoryInteraction() {
    Collection<String> roles = Arrays.asList("ROLE1", "ROLE2");

    portalTeamAccessor.addPersonToTeamWithRoles(TEAM_RES_ID, targetPerson, roles, actionPerformedBy);
    verify(portalTeamRepository, times(1)).updateUserRoles(
        TEAM_RES_ID,
        targetPerson.getId().asInt(),
        "ROLE1,ROLE2",
        actionPerformedBy.getWuaId()
    );
  }

  @Test(expected = RuntimeException.class)
  public void addPersonToTeamWithRoles_caughtErrorsAreRethrown() {
    doThrow(new NullPointerException()).when(portalTeamRepository).updateUserRoles(any(), any(), any(), any());
    Collection<String> roles = Arrays.asList("ROLE1", "ROLE2");
    portalTeamAccessor.addPersonToTeamWithRoles(TEAM_RES_ID, targetPerson, roles, actionPerformedBy);

  }

  @Test(expected = RuntimeException.class)
  public void createOrganisationGroupTeam_caughtExceptionIsRethrown() {
    doThrow(new NullPointerException()).when(portalTeamRepository).createTeam(any(), any(), any(), any(), any());
    portalTeamAccessor.createOrganisationGroupTeam(new PortalOrganisationGroup(), user);
  }

  @Test
  public void createOrganisationGroupTeam_verifyRepositoryInteraction() {
    portalTeamAccessor.createOrganisationGroupTeam(new PortalOrganisationGroup(), user);
    verify(portalTeamRepository, times(1)).createTeam(any(), any(), any(), any(), any());
  }
}
