package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.HuooForm;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.TestHarnessOrganisationUnitService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
@Profile("test-harness")
class PadHuooGeneratorService implements TestHarnessAppFormService {

  private final TestHarnessOrganisationUnitService testHarnessOrganisationUnitService;
  private final PadOrganisationRoleService padOrganisationRoleService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.HUOO;

  @Autowired
  public PadHuooGeneratorService(
      TestHarnessOrganisationUnitService testHarnessOrganisationUnitService,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.testHarnessOrganisationUnitService = testHarnessOrganisationUnitService;
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

    var orgUnit = testHarnessOrganisationUnitService
        .getFirstOrgUnitUserCanAccessOrThrow(appFormServiceParams.getUser());

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

}
