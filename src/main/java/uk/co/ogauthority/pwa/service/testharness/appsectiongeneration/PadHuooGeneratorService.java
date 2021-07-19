package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
@Profile("development")
public class PadHuooGeneratorService {


  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final TeamService teamService;
  private final PadOrganisationRolesRepository padOrganisationRolesRepository;

  @Autowired
  public PadHuooGeneratorService(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      TeamService teamService,
      PadOrganisationRolesRepository padOrganisationRolesRepository) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.teamService = teamService;
    this.padOrganisationRolesRepository = padOrganisationRolesRepository;
  }


  /**
   * Get the first org unit the user has access to and set that for each huoo role on the application detail.
   */
  public void generatePadOrgRoles(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var orgUnit = getOrgUnitsUserCanAccess(user.getLinkedPerson()).stream()
        .findFirst().orElseThrow(() -> new IllegalStateException(
            String.format("User with WUA ID: %s does not have access to any organisation units", user.getWuaId())));

    var padOrgRoles = HuooRole.stream()
        .map(huooRole -> {
          var padOrganisationRole = new PadOrganisationRole();
          padOrganisationRole.setAgreement(null);
          padOrganisationRole.setPwaApplicationDetail(pwaApplicationDetail);
          padOrganisationRole.setRole(huooRole);
          padOrganisationRole.setType(HuooType.PORTAL_ORG);
          padOrganisationRole.setOrganisationUnit(orgUnit);
          return padOrganisationRole;
        })
        .collect(Collectors.toList());

    padOrganisationRolesRepository.saveAll(padOrgRoles);
  }


  private List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(Person person) {

    var orgGroupsUserCanAccess = teamService.getOrganisationTeamListIfPersonInRole(
        person,
        List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());

    return portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(orgGroupsUserCanAccess);
  }






}
