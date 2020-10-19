package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.huoo.EditHuooValidator;
import uk.co.ogauthority.pwa.validators.huoo.HuooValidationView;

@RunWith(MockitoJUnitRunner.class)
public class EditHuooValidatorTest {

  @Mock
  private PadOrganisationRoleService organisationRoleService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private TeamService teamService;

  private EditHuooValidator validator;

  private PwaApplicationDetail detail;
  private List<PadOrganisationRole> portalOrgRoles;
  private List<PadOrganisationRole> treatyOrgRoles;
  private PortalOrganisationUnit orgUnit;

  @Before
  public void setUp() {

    detail = new PwaApplicationDetail();
    detail.setNumOfHolders(2);

    var portalOrgRole = new PadOrganisationRole();
    portalOrgRole.setType(HuooType.PORTAL_ORG);
    portalOrgRole.setRole(HuooRole.HOLDER);

    orgUnit = PortalOrganisationTestUtils.getOrganisationUnit();

    when(portalOrganisationsAccessor.getOrganisationUnitById(orgUnit.getOuId())).thenReturn(Optional.of(orgUnit));

    portalOrgRole.setOrganisationUnit(orgUnit);

    var treatyOrgRole = new PadOrganisationRole();
    treatyOrgRole.setType(HuooType.TREATY_AGREEMENT);
    treatyOrgRole.setRole(HuooRole.USER);
    treatyOrgRole.setAgreement(TreatyAgreement.ANY_TREATY_COUNTRY);

    portalOrgRoles = List.of(portalOrgRole);
    treatyOrgRoles = List.of(treatyOrgRole);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(portalOrgRoles);
    validator = new EditHuooValidator(organisationRoleService, portalOrganisationsAccessor, teamService);
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
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(treatyOrgRoles));

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_mandatory_huooType_portalOrg() {

    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of());
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.required", "organisationUnitId.invalid")),
        entry("huooRoles", Set.of("huooRoles.required", "huooRoles.requiresOneHolder"))
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
  public void invalid_huooType_portalOrg_lastHolder() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.OWNER));
    form.setOrganisationUnitId(orgUnit.getOuId());

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    assertThat(result).containsOnly(
        entry("huooRoles", Set.of("huooRoles.requiresOneHolder"))
    );

  }


  @Test
  public void unitSelectedIsPartOfUsersOrg_valid() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(1);
    form.setHuooRoles(Set.of());
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.USER)
    );
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(new PortalOrganisationUnit(1, "name")));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles), new AuthenticatedUserAccount(new WebUserAccount(), List.of(PwaUserPrivilege.PWA_MANAGER)));

    assertThat(result).doesNotContain(
        entry("organisationUnitId", Set.of("organisationUnitId.invalid"))
    );
  }


  /**
   * GIVEN:
   *  Org to edit previously had HOLDER role.
   *  Org to edit still has HOLDER role.
   *  Org to edit is not owned by the organisation performing the edit.
   * EXPECT:
   *  Validation error
   */
  @Test
  public void unitSelectedIsPartOfUsersOrg_invalid_stillHolder() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(1);
    form.setHuooRoles(Set.of(HuooRole.HOLDER));
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.HOLDER)
    );
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(new PortalOrganisationUnit(1, "name")));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(new PortalOrganisationUnit(2, "name")));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles), new AuthenticatedUserAccount(new WebUserAccount(), List.of(PwaUserPrivilege.PWA_MANAGER)));

    assertThat(result).contains(
        entry("organisationUnitId", Set.of("organisationUnitId.invalid"))
    );
  }

  /**
   * GIVEN:
   *  Org to edit previously had HOLDER role.
   *  Org to edit no longer has a HOLDER role.
   *  Org to edit is not owned by the organisation performing the edit.
   * EXPECT:
   *  Validation error
   */
  @Test
  public void unitSelectedIsPartOfUsersOrg_invalid_wasHolder() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(1);
    form.setHuooRoles(Set.of());
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.HOLDER)
    );
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(new PortalOrganisationUnit(1, "name")));
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(new PortalOrganisationUnit(2, "name")));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles), new AuthenticatedUserAccount(new WebUserAccount(), List.of(PwaUserPrivilege.PWA_MANAGER)));

    assertThat(result).contains(
        entry("organisationUnitId", Set.of("organisationUnitId.invalid"))
    );
  }

  /**
   * GIVEN:
   *  Org to edit previously did not have a HOLDER role.
   *  Org to edit now has a HOLDER role.
   *  Org to edit is not owned by the organisation performing the edit.
   * EXPECT:
   *  Validation error
   */
  @Test
  public void unitSelectedIsPartOfUsersOrg_invalid_nonHolder() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(1);
    form.setHuooRoles(Set.of(HuooRole.USER, HuooRole.OPERATOR, HuooRole.OWNER));
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.USER)
    );
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(new PortalOrganisationUnit(1, "name")));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles), new AuthenticatedUserAccount(new WebUserAccount(), List.of(PwaUserPrivilege.PWA_MANAGER)));

    assertThat(result).doesNotContainKeys("organisationUnitId");
  }

  @Test
  public void unitSelectedIsPartOfUsersOrg_variationPwa() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(1);
    form.setHuooRoles(Set.of());
    var application = new PwaApplication(null, PwaApplicationType.HUOO_VARIATION, null);
    detail.setPwaApplication(application);

    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(new PortalOrganisationUnit(1, "name")));
    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles), new AuthenticatedUserAccount(new WebUserAccount(), List.of(PwaUserPrivilege.PWA_MANAGER)));

    assertThat(result).doesNotContain(
        entry("organisationUnitId", Set.of("organisationUnitId.invalid"))
    );
  }


  @Test
  public void validateHolderCountLessThanOverallPwa_invalid() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of(HuooRole.HOLDER));

    detail.setNumOfHolders(1);
    var padOrgRole1 = new PadOrganisationRole();
    padOrgRole1.setRole(HuooRole.HOLDER);
    padOrgRole1.setType(HuooType.PORTAL_ORG);
    var orgUnit = new PortalOrganisationUnit(1, "");
    padOrgRole1.setOrganisationUnit(orgUnit);
    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(padOrgRole1));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    assertThat(result).contains(
        entry("huooRoles", Set.of("huooRoles.alreadyUsed"))
    );
  }


  @Test
  public void validateHolderCountLessThanOverallPwa_valid() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of(HuooRole.HOLDER));

    var padOrgRole1 = new PadOrganisationRole();
    padOrgRole1.setRole(HuooRole.HOLDER);
    padOrgRole1.setType(HuooType.PORTAL_ORG);
    var orgUnit = new PortalOrganisationUnit(1, "");
    padOrgRole1.setOrganisationUnit(orgUnit);
    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(padOrgRole1));

    var result = ValidatorTestUtils.getFormValidationErrors(validator, form, detail, getValidationView(portalOrgRoles));

    assertThat(result).doesNotContain(
        entry("huooRoles", Set.of("huooRoles.alreadyUsed"))
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
