package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.repo.PortalTeamRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

/**
 * Majority of service tests should be in the integration test {@link uk.co.ogauthority.pwa.integration.energyportal.teams.PortalTeamAccessorIntegrationTest}
 */
@ExtendWith(MockitoExtension.class)
class PortalTeamAccessorTest {

  private final int TEAM_RES_ID = 12345;

  @Mock
  private PortalTeamRepository portalTeamRepository;

  @Mock
  private EntityManager entityManager;

  private Person targetPerson;
  private WebUserAccount actionPerformedBy;
  private AuthenticatedUserAccount user;

  private PortalTeamAccessor portalTeamAccessor;


  @BeforeEach
  void setup() {
    portalTeamAccessor = new PortalTeamAccessor(portalTeamRepository, entityManager);

    targetPerson = new Person(1, "fname", "sname", "email", "0");
    actionPerformedBy = new WebUserAccount(9);
    user = new AuthenticatedUserAccount(actionPerformedBy, List.of());
  }


  @Test
  void removePersonFromTeam_verifyRepositoryInteraction() {
    portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, targetPerson, actionPerformedBy);
    verify(portalTeamRepository, times(1)).removeUserFromTeam(
        TEAM_RES_ID,
        targetPerson.getId().asInt(),
        actionPerformedBy.getWuaId()
    );
  }


  @Test
  void removePersonFromTeam_caughtErrorsAreRethrown() {
    doThrow(new NullPointerException()).when(portalTeamRepository).removeUserFromTeam(any(), any(), any());
    assertThrows(RuntimeException.class, () ->
      portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, targetPerson, actionPerformedBy));
  }

  @Test
  void addPersonToTeamWithRoles_verifyRepositoryInteraction() {
    Collection<String> roles = Arrays.asList("ROLE1", "ROLE2");

    portalTeamAccessor.addPersonToTeamWithRoles(TEAM_RES_ID, targetPerson, roles, actionPerformedBy);
    verify(portalTeamRepository, times(1)).updateUserRoles(
        TEAM_RES_ID,
        "ROLE1,ROLE2",
        targetPerson.getId().asInt(),
        actionPerformedBy.getWuaId()
    );
  }

  @Test
  void addPersonToTeamWithRoles_caughtErrorsAreRethrown() {
    doThrow(new NullPointerException()).when(portalTeamRepository).updateUserRoles(any(), any(), any(), any());
    Collection<String> roles = Arrays.asList("ROLE1", "ROLE2");
    assertThrows(RuntimeException.class, () ->
      portalTeamAccessor.addPersonToTeamWithRoles(TEAM_RES_ID, targetPerson, roles, actionPerformedBy));

  }

  @Test
  void createOrganisationGroupTeam_caughtExceptionIsRethrown() {
    doThrow(new NullPointerException()).when(portalTeamRepository).createTeam(any(), any(), any(), any(), any());
    assertThrows(RuntimeException.class, () ->
      portalTeamAccessor.createOrganisationGroupTeam(new PortalOrganisationGroup(), user));
  }

  @Test
  void createOrganisationGroupTeam_verifyRepositoryInteraction() {
    portalTeamAccessor.createOrganisationGroupTeam(new PortalOrganisationGroup(), user);
    verify(portalTeamRepository, times(1)).createTeam(any(), any(), any(), any(), any());
  }
}
