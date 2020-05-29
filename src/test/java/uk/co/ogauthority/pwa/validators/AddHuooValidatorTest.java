package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.huoo.AddHuooValidator;

@RunWith(MockitoJUnitRunner.class)
public class AddHuooValidatorTest {

  @Mock
  private PadOrganisationRoleService organisationRoleService;

  private AddHuooValidator validator;

  private PwaApplicationDetail detail;
  private List<PadOrganisationRole> orgRoles;

  @Before
  public void setUp() {

    detail = new PwaApplicationDetail();
    orgRoles = List.of();

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(orgRoles);
    validator = new AddHuooValidator(organisationRoleService);
  }

  @Test
  public void valid_portalOrg_dataPresent() {

    var form = buildForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void valid_treaty_dataPresent() {

    var form = new HuooForm();
    form.setHuooType(HuooType.TREATY_AGREEMENT);
    form.setTreatyAgreement(TreatyAgreement.BELGIUM);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_mandatory_huooTypeNotPresent() {

    var form = new HuooForm();
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    assertThat(result).containsOnly(
        entry("huooType", Set.of("huooType.required"))
    );

  }

  @Test
  public void invalid_mandatory_huooType_portalOrg() {

    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.required")),
        entry("huooRoles", Set.of("huooRoles.required"))
    );

  }

  @Test
  public void invalid_mandatory_huooType_treaty() {

    var form = new HuooForm();
    form.setHuooType(HuooType.TREATY_AGREEMENT);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    assertThat(result).containsOnly(
        entry("treatyAgreement", Set.of("treatyAgreement.required"))
    );

  }

  @Test
  public void invalid_huooType_portalOrg_duplicate() {

    var form = buildForm();

    var orgRole = new PadOrganisationRole();
    var ou = PortalOrganisationTestUtils.generateOrganisationUnit(form.getOrganisationUnitId(), "org", null);
    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setOrganisationUnit(ou);
    orgRole.setRole(HuooRole.OWNER);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(orgRole));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.alreadyUsed"))
    );

  }

  @Test
  public void invalid_huooType_treaty_duplicate() {

    var form = new HuooForm();
    form.setHuooType(HuooType.TREATY_AGREEMENT);
    form.setTreatyAgreement(TreatyAgreement.BELGIUM);

    var orgRole = new PadOrganisationRole();
    orgRole.setType(HuooType.TREATY_AGREEMENT);
    orgRole.setAgreement(TreatyAgreement.BELGIUM);
    orgRole.setRole(HuooRole.OWNER);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(orgRole));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    assertThat(result).containsOnly(
        entry("treatyAgreement", Set.of("treatyAgreement.duplicate"))
    );

  }

  @Test
  public void invalid_huooType_portalOrg_holderNotAllowed() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER));

    var orgRole = new PadOrganisationRole();
    var ou = PortalOrganisationTestUtils.generateOrganisationUnit(111, "org", null);

    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setRole(HuooRole.HOLDER);
    orgRole.setOrganisationUnit(ou);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(orgRole));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);

    assertThat(result).containsOnly(
        entry("huooRoles", Set.of("huooRoles.holderNotAllowed"))
    );

  }

  private HuooForm buildForm() {

    var form = new HuooForm();

    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(2);
    form.setHuooRoles(Set.of(HuooRole.OPERATOR, HuooRole.USER, HuooRole.OWNER));

    return form;

  }

}
