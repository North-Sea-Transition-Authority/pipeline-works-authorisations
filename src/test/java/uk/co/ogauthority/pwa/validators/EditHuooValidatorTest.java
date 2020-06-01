package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.huoo.EditHuooValidator;
import uk.co.ogauthority.pwa.validators.huoo.HuooValidationView;

@RunWith(MockitoJUnitRunner.class)
public class EditHuooValidatorTest {

  @Mock
  private PadOrganisationRoleService organisationRoleService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  private EditHuooValidator validator;

  private PwaApplicationDetail detail;
  private List<PadOrganisationRole> portalOrgRoles;
  private List<PadOrganisationRole> treatyOrgRoles;
  private PortalOrganisationUnit orgUnit;

  @Before
  public void setUp() {

    detail = new PwaApplicationDetail();

    var portalOrgRole = new PadOrganisationRole();
    portalOrgRole.setType(HuooType.PORTAL_ORG);
    portalOrgRole.setRole(HuooRole.HOLDER);

    orgUnit = PortalOrganisationTestUtils.getOrganisationUnit();

    when(portalOrganisationsAccessor.getOrganisationUnitById(orgUnit.getOuId())).thenReturn(Optional.of(orgUnit));

    portalOrgRole.setOrganisationUnit(orgUnit);

    var treatyOrgRole = new PadOrganisationRole();
    treatyOrgRole.setType(HuooType.TREATY_AGREEMENT);
    treatyOrgRole.setRole(HuooRole.USER);
    treatyOrgRole.setAgreement(TreatyAgreement.IRELAND);

    portalOrgRoles = List.of(portalOrgRole);
    treatyOrgRoles = List.of(treatyOrgRole);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(portalOrgRoles);
    validator = new EditHuooValidator(organisationRoleService, portalOrganisationsAccessor);
  }

  @Test
  public void valid_portalOrg_dataPresent() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER, HuooRole.OWNER));
    form.setOrganisationUnitId(portalOrgRoles.get(0).getOrganisationUnit().getOuId());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void valid_treaty_dataPresent() {

    var form = new HuooForm();
    form.setHuooType(HuooType.TREATY_AGREEMENT);
    form.setTreatyAgreement(TreatyAgreement.BELGIUM);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(treatyOrgRoles));

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_mandatory_huooType_portalOrg() {

    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.required", "organisationUnitId.invalid")),
        entry("huooRoles", Set.of("huooRoles.required", "huooRoles.requiresOneHolder"))
    );

  }

  @Test
  public void invalid_mandatory_huooType_treaty() {

    var form = new HuooForm();
    form.setHuooType(HuooType.TREATY_AGREEMENT);
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(treatyOrgRoles));

    assertThat(result).containsOnly(
        entry("treatyAgreement", Set.of("treatyAgreement.required"))
    );

  }

  @Test
  public void invalid_huooType_portalOrg_duplicate() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER));

    var orgRole = new PadOrganisationRole();
    var ou = PortalOrganisationTestUtils.generateOrganisationUnit(99, "org", null);
    when(portalOrganisationsAccessor.getOrganisationUnitById(ou.getOuId())).thenReturn(Optional.of(ou));

    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setOrganisationUnit(ou);
    orgRole.setRole(HuooRole.OWNER);

    var validationView = getValidationView(List.of(portalOrgRoles.get(0)));

    portalOrgRoles = new ArrayList<>(portalOrgRoles);
    portalOrgRoles.add(orgRole);

    form.setOrganisationUnitId(99);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(portalOrgRoles);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, validationView);

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

    var validationView = getValidationView(treatyOrgRoles);

    treatyOrgRoles = new ArrayList<>(treatyOrgRoles);
    treatyOrgRoles.add(orgRole);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(treatyOrgRoles);

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, validationView);

    assertThat(result).containsOnly(
        entry("treatyAgreement", Set.of("treatyAgreement.duplicate"))
    );

  }

  @Test
  public void invalid_huooType_portalOrg_lastHolder() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.OWNER));
    form.setOrganisationUnitId(orgUnit.getOuId());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    assertThat(result).containsOnly(
        entry("huooRoles", Set.of("huooRoles.requiresOneHolder"))
    );

  }

  private HuooForm buildForm() {

    var form = new HuooForm();

    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(2);
    form.setHuooRoles(Set.of(HuooRole.OPERATOR, HuooRole.USER, HuooRole.OWNER));

    return form;

  }

  private HuooValidationView getValidationView(List<PadOrganisationRole> orgRoles) {
    return new HuooValidationView(new HashSet<>(orgRoles));
  }

}
