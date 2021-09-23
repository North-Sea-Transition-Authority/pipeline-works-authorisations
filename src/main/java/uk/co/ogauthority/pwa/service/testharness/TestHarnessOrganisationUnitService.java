package uk.co.ogauthority.pwa.service.testharness;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Profile("test-harness")
@Service
public class TestHarnessOrganisationUnitService {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final TeamService teamService;

  @Autowired
  public TestHarnessOrganisationUnitService(PortalOrganisationsAccessor portalOrganisationsAccessor,
                                            TeamService teamService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.teamService = teamService;
  }

  public PortalOrganisationUnit getFirstOrgUnitUserCanAccessOrThrow(WebUserAccount webUserAccount) {

    var orgGroupsUserCanAccess = teamService.getOrganisationTeamListIfPersonInRole(
            webUserAccount.getLinkedPerson(),
            List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());

    return portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(orgGroupsUserCanAccess).stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(String.format(
            "User with WUA ID: %s does not have access to any organisation units", webUserAccount.getWuaId())));

  }

}
