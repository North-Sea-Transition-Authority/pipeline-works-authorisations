package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;

@Service
@Profile("test-harness")
class PadHuooGeneratorService implements TestHarnessAppFormService {


  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final TeamService teamService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.HUOO;

  @Autowired
  public PadHuooGeneratorService(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      TeamService teamService,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.teamService = teamService;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }



  /**
   * Get the first org unit the user has access to and use that as the selected org unit for the org roles.
   */
  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var orgUnit = getOrgUnitsUserCanAccess(appFormServiceParams.getUser().getLinkedPerson()).stream()
        .findFirst().orElseThrow(() -> new IllegalStateException(String.format(
            "User with WUA ID: %s does not have access to any organisation units", appFormServiceParams.getUser().getWuaId())));

    var huooRolesToAdd = getHuooRolesToAdd(appFormServiceParams.getApplicationDetail());
    if (!huooRolesToAdd.isEmpty()) {
      var form = createForm(orgUnit, huooRolesToAdd);
      padOrganisationRoleService.saveEntityUsingForm(appFormServiceParams.getApplicationDetail(), form);
    }

  }

  private Set<HuooRole> getHuooRolesToAdd(PwaApplicationDetail detail) {
    var existingHuooRolesOnApp = padOrganisationRoleService.getOrgRolesForDetail(detail)
        .stream().map(PadOrganisationRole::getRole).collect(Collectors.toSet());
    return SetUtils.difference(EnumSet.allOf(HuooRole.class), existingHuooRolesOnApp);
  }


  private HuooForm createForm(PortalOrganisationUnit portalOrganisationUnit, Set<HuooRole> huooRoles) {
    var form = new HuooForm();
    form.setHuooRoles(huooRoles);
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(portalOrganisationUnit.getOuId());
    return form;
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
