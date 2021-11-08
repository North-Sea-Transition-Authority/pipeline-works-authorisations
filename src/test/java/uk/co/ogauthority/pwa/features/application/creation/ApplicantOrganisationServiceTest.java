package uk.co.ogauthority.pwa.features.application.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantOrganisationServiceTest {

  @Mock
  private PwaHolderService pwaHolderService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  private ApplicantOrganisationService applicantOrganisationService;

  private PortalOrganisationUnit linkedOrg1, linkedOrg2, separateOrg;

  private final MasterPwa masterPwa = new MasterPwa();
  private final WebUserAccount webUserAccount = new WebUserAccount();

  @Before
  public void setUp() {

    applicantOrganisationService = new ApplicantOrganisationService(pwaHolderService, pwaHolderTeamService);

    linkedOrg1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Link1");
    linkedOrg2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "Link3");
    separateOrg = PortalOrganisationTestUtils.generateOrganisationUnit(3, "Separate");

    when(pwaHolderService.getPwaHolderOrgUnits(masterPwa)).thenReturn(Set.of(linkedOrg1, linkedOrg2, separateOrg));

  }

  @Test
  public void getPotentialApplicantOrganisations_userIsInAHolderTeam_onlyRelevantOrgsReturned() {

    when(pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasOrgRole(webUserAccount, PwaOrganisationRole.APPLICATION_CREATOR))
        .thenReturn(List.of(linkedOrg1, linkedOrg2));

    var orgs = applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, webUserAccount);

    assertThat(orgs).containsExactlyInAnyOrder(linkedOrg1, linkedOrg2);

  }

  @Test
  public void getPotentialApplicantOrganisations_userNotInAnyRelevantTeams_nothingReturned() {

    when(pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasOrgRole(webUserAccount, PwaOrganisationRole.APPLICATION_CREATOR))
        .thenReturn(List.of());

    var orgs = applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, webUserAccount);

    assertThat(orgs).isEmpty();

  }

}