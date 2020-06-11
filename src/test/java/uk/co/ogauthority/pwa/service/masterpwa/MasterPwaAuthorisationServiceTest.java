package uk.co.ogauthority.pwa.service.masterpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaRepository;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaAuthorisationService;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaAuthorisationServiceTest {

  private static final int MASTER_PWA_ID = 10;

  @Mock
  private MasterPwaRepository masterPwaRepository;

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  @Mock
  private TeamService teamService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private MasterPwa masterPwa;

  @Mock
  private PwaConsent pwaConsent;

  @Mock
  private PortalOrganisationGroup orgTeamOrgGroup;

  @Mock
  private PortalOrganisationUnit teamOrgGrpOrgUnit;

  @Mock
  private PwaOrganisationTeam orgTeam;

  private MasterPwaAuthorisationService masterPwaAuthorisationService;

  private Person userPerson = new Person();
  private WebUserAccount webUserAccount = new WebUserAccount(1, userPerson);


  @Before
  public void setup() {

    when(pwaConsent.getMasterPwa()).thenReturn(masterPwa);

    when(teamOrgGrpOrgUnit.getPortalOrganisationGroup()).thenReturn(orgTeamOrgGroup);
    when(orgTeam.getPortalOrganisationGroup()).thenReturn(orgTeamOrgGroup);
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(
        List.of(teamOrgGrpOrgUnit));


    when(teamService.getOrganisationTeamListIfPersonInRole(eq(userPerson), any())).thenReturn(List.of(orgTeam));

    masterPwaAuthorisationService = new MasterPwaAuthorisationService(
        masterPwaRepository,
        masterPwaDetailRepository,
        teamService,
        portalOrganisationsAccessor,
        pwaConsentOrganisationRoleService);

    when(masterPwaRepository.findById(MASTER_PWA_ID)).thenReturn(Optional.of(masterPwa));
  }


  @Test
  public void getMasterPwaIfAuthorised_whenUserInHolderTeamforPwa() {

    var singleHolder = new MasterPwaHolderDto(teamOrgGrpOrgUnit, pwaConsent);
    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of(singleHolder));

    assertThat(masterPwaAuthorisationService.getMasterPwaIfAuthorised(MASTER_PWA_ID, webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR))
        .isEqualTo(masterPwa);

    verify(teamService, times(1))
        .getOrganisationTeamListIfPersonInRole(userPerson, Set.of(PwaOrganisationRole.APPLICATION_CREATOR));

  }

  @Test(expected = AccessDeniedException.class)
  public void getMasterPwaIfAuthorised_whenUserNotInHolderTeamforPwa() {

    var otherOrgGrp = mock(PortalOrganisationGroup.class);
    var otherOrgUnit = mock(PortalOrganisationUnit.class);

    var singleHolder = new MasterPwaHolderDto(otherOrgUnit, pwaConsent);
    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of(singleHolder));

    masterPwaAuthorisationService.getMasterPwaIfAuthorised(MASTER_PWA_ID, webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getMasterPwaIfAuthorised_whenMasterPwaNotFound() {
    masterPwaAuthorisationService.getMasterPwaIfAuthorised(9999, webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR);
  }


  @Test
  public void getMasterPwasWhereUserIsAuthorised_whenUserOrgUnitsCoverSingleConsent(){

    when(pwaConsentOrganisationRoleService.getPwaConsentsWhereCurrentHolderWasAdded(any()))
    .thenReturn(Set.of(pwaConsent));

    var authorisedPwas = masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(
        webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(authorisedPwas).containsExactly(masterPwa);

    verify(teamService, times(1))
        .getOrganisationTeamListIfPersonInRole(userPerson, Set.of(PwaOrganisationRole.APPLICATION_CREATOR));
  }

  @Test
  public void getMasterPwasWhereUserIsAuthorised_whenUserOrgUnitsCoverNoConsent(){

    var authorisedPwas = masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(
        webUserAccount,
        PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(authorisedPwas).isEmpty();

  }


}
