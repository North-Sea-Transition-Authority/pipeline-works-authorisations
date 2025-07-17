package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import uk.co.fivium.energyportal.accounts.starter.EnergyPortalServiceAccessService;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.repo.PortalTeamRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountTestUtil;

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

  @Mock
  private EnergyPortalServiceAccessService energyPortalServiceAccessService;

  @Mock
  private Environment environment;

  @Mock
  private UserAccountService userAccountService;

  private Person targetPerson;
  private WebUserAccount actionPerformedBy;
  private AuthenticatedUserAccount user;

  private PortalTeamAccessor portalTeamAccessor;


  @Before
  public void setup() {

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(false);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

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

  @Test
  public void addPersonToTeamWithRoles_whenNoPreviousAccess_andFoxIdp() {

    var personToAdd = PersonTestUtil.createDefaultPerson();
    var actionedByWua = WebUserAccountTestUtil.createWebUserAccount(
        1,
        personToAdd,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(false);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    doReturn(false)
        .when(portalTeamAccessor).hasAccessToService(personToAdd);

    portalTeamAccessor.addPersonToTeamWithRoles(
        TEAM_RES_ID,
        personToAdd,
        List.of("ROLE_NAME_1", "ROLE_NAME_2"),
        actionedByWua
    );

    verify(portalTeamRepository).updateUserRoles(
        TEAM_RES_ID,
        "ROLE_NAME_1,ROLE_NAME_2",
        personToAdd.getId().asInt(),
        actionedByWua.getWuaId()
    );

    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
  }

  @Test
  public void addPersonToTeamWithRoles_whenNoPreviousAccess_andNonFoxIdp() {

    var personToAdd = PersonTestUtil.createDefaultPerson();
    var wuaOfPersonToAdd = WebUserAccountTestUtil.createWebUserAccount(
        1,
        personToAdd,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    var actionedByWua = WebUserAccountTestUtil.createWebUserAccount(
        2,
        personToAdd,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(true);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    doReturn(false)
        .when(portalTeamAccessor).hasAccessToService(personToAdd);

    when(userAccountService.findByPerson(personToAdd))
        .thenReturn(Optional.of(wuaOfPersonToAdd));

    portalTeamAccessor.addPersonToTeamWithRoles(
        TEAM_RES_ID,
        personToAdd,
        List.of("ROLE_NAME_1", "ROLE_NAME_2"),
        actionedByWua
    );

    verify(portalTeamRepository).updateUserRoles(
        TEAM_RES_ID,
        "ROLE_NAME_1,ROLE_NAME_2",
        personToAdd.getId().asInt(),
        actionedByWua.getWuaId()
    );

    verify(energyPortalServiceAccessService).addUser(1);

  }

  @Test
  public void addPersonToTeamWithRoles_whenPreviousAccess_andNonFoxIdp() {

    var personToAdd = PersonTestUtil.createDefaultPerson();

    var actionedByWua = WebUserAccountTestUtil.createWebUserAccount(
        1,
        personToAdd,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(true);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    doReturn(true)
        .when(portalTeamAccessor).hasAccessToService(personToAdd);

    portalTeamAccessor.addPersonToTeamWithRoles(
        TEAM_RES_ID,
        personToAdd,
        List.of("ROLE_NAME_1", "ROLE_NAME_2"),
        actionedByWua
    );

    verify(portalTeamRepository).updateUserRoles(
        TEAM_RES_ID,
        "ROLE_NAME_1,ROLE_NAME_2",
        personToAdd.getId().asInt(),
        actionedByWua.getWuaId()
    );

    verify(energyPortalServiceAccessService, never()).addUser(anyLong());
  }

  @Test
  public void addPersonToTeamWithRoles_whenException_thenRethrown() {

    var personToAdd = PersonTestUtil.createDefaultPerson();

    var actionedByWua = WebUserAccountTestUtil.createWebUserAccount(
        1,
        personToAdd,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(true);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    doReturn(false)
        .when(portalTeamAccessor).hasAccessToService(personToAdd);

    doThrow(new IllegalStateException("unexpected error"))
        .when(portalTeamRepository).updateUserRoles(any(), any(), any(), any());

    assertThatThrownBy(() -> portalTeamAccessor.addPersonToTeamWithRoles(
        TEAM_RES_ID,
        personToAdd,
        List.of("ROLE_NAME_1", "ROLE_NAME_2"),
        actionedByWua
    ))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error adding person to team");
  }

  @Test
  public void removePersonFromTeam_whenStillHasAccess_andFoxIdp() {

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(false);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    var personToRemove = PersonTestUtil.createDefaultPerson();

    var actionPerformedBy = WebUserAccountTestUtil.createWebUserAccount(
        1,
        personToRemove,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, personToRemove, actionPerformedBy);

    verify(portalTeamRepository)
        .removeUserFromTeam(TEAM_RES_ID, personToRemove.getId().asInt(), actionPerformedBy.getWuaId());

    verify(energyPortalServiceAccessService, never()).removeUser(anyLong());

  }

  @Test
  public void removePersonFromTeam_whenStillHasAccess_andNonFoxIdp() {

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(true);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    var personToRemove = PersonTestUtil.createDefaultPerson();

    var actionPerformedBy = WebUserAccountTestUtil.createWebUserAccount(
        1,
        PersonTestUtil.createDefaultPerson(),
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    doReturn(true).when(portalTeamAccessor).hasAccessToService(personToRemove);

    portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, personToRemove, actionPerformedBy);

    verify(portalTeamRepository)
        .removeUserFromTeam(TEAM_RES_ID, personToRemove.getId().asInt(), actionPerformedBy.getWuaId());

    verify(energyPortalServiceAccessService, never()).removeUser(anyLong());
  }

  @Test
  public void removePersonFromTeam_whenNoLongerHasAccess_andNonFoxIdp() {

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(true);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    var personToRemove = PersonTestUtil.createDefaultPerson();
    var personToRemoveWua = WebUserAccountTestUtil.createWebUserAccount(
        1,
        personToRemove,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    var actionPerformedBy = WebUserAccountTestUtil.createWebUserAccount(
        2,
        PersonTestUtil.createDefaultPerson(),
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    doReturn(false).when(portalTeamAccessor).hasAccessToService(personToRemove);

    when(userAccountService.findByPerson(personToRemove))
        .thenReturn(Optional.of(personToRemoveWua));

    portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, personToRemove, actionPerformedBy);

    verify(portalTeamRepository)
        .removeUserFromTeam(TEAM_RES_ID, personToRemove.getId().asInt(), actionPerformedBy.getWuaId());

    verify(energyPortalServiceAccessService).removeUser(personToRemoveWua.getWuaId());
  }

  @Test
  public void removePersonFromTeam_whenUnexpectedError_thenRethrown() {

    when(environment.matchesProfiles("use-epas"))
        .thenReturn(false);

    portalTeamAccessor = spy(new PortalTeamAccessor(
        portalTeamRepository,
        entityManager,
        energyPortalServiceAccessService,
        userAccountService,
        environment
    ));

    var personToRemove = PersonTestUtil.createDefaultPerson();

    var actionPerformedBy = WebUserAccountTestUtil.createWebUserAccount(
        2,
        PersonTestUtil.createDefaultPerson(),
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    doThrow(new IllegalStateException("test")).when(portalTeamRepository)
        .removeUserFromTeam(TEAM_RES_ID, personToRemove.getId().asInt(), actionPerformedBy.getWuaId());

    assertThatThrownBy(() -> portalTeamAccessor.removePersonFromTeam(TEAM_RES_ID, personToRemove, actionPerformedBy))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Error Removing person from team.");

  }
}
