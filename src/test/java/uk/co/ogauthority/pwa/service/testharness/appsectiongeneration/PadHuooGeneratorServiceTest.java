package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadHuooGeneratorServiceTest {


  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private TeamService teamService;

  @Mock
  private PadOrganisationRolesRepository padOrganisationRolesRepository;

  PadHuooGeneratorService padHuooGeneratorService;

  @Before
  public void setup(){
    padHuooGeneratorService = new PadHuooGeneratorService(
        portalOrganisationsAccessor, teamService, padOrganisationRolesRepository);
  }


  @Test
  public void generatePadOrgRoles_verifyEntitiesSaved()  {

    var user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());
    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

    var portalOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "", "");
    var orgTeam = new PwaOrganisationTeam(1, "", "", portalOrgGroup);
    when(teamService.getOrganisationTeamListIfPersonInRole(any(), anyList())).thenReturn(List.of(orgTeam));

    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "", portalOrgGroup);
    when(portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(anyList())).thenReturn(List.of(orgUnit));

    padHuooGeneratorService.generatePadOrgRoles(user, pwaApplicationDetail);

    verify(padOrganisationRolesRepository).saveAll(anyList());
  }

  
  
  
}
