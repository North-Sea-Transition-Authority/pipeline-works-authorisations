package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class EditHuooValidatorTest {
  
  private static final OrganisationUnitId ORG_UNIT_1_ID = OrganisationUnitId.fromInt(1);

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

  private AuthenticatedUserAccount authenticatedUserAccount;

  @Before
  public void setUp() {

    var person = PersonTestUtil.createDefaultPerson();
    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, person), EnumSet.allOf(PwaUserPrivilege.class));
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    detail.setNumOfHolders(2);

    var portalOrgRole = new PadOrganisationRole();
    portalOrgRole.setType(HuooType.PORTAL_ORG);
    portalOrgRole.setRole(HuooRole.HOLDER);

    orgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();

    when(organisationRoleService.organisationExistsAndActive(any())).thenReturn(true);

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
  public void invalid_appIsInitialPwa_portalOrgAccessibleForUser_organisationIsActive() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.INITIAL);
    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER, HuooRole.OWNER));
    form.setOrganisationUnitId(portalOrgRoles.get(0).getOrganisationUnit().getOuId());

    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgUnit));

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_appIsInitialPwa_portalOrgAccessibleForUser_organisationIsNotActive() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.INITIAL);
    var portalOrgRole = new PadOrganisationRole();
    portalOrgRole.setType(HuooType.PORTAL_ORG);
    portalOrgRole.setRole(HuooRole.HOLDER);

    orgUnit = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();
    portalOrgRole.setOrganisationUnit(orgUnit);

    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any())).thenReturn(List.of(orgUnit));

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER, HuooRole.OWNER));
    form.setOrganisationUnitId(orgUnit.getOuId());

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.orgUnitNotActive"))
    );

  }

  @Test
  public void valid_treaty_dataPresent() {

    var form = new HuooForm();
    form.setHuooType(HuooType.TREATY_AGREEMENT);

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(treatyOrgRoles), authenticatedUserAccount);

    verify(organisationRoleService, times(1)).getOrgRolesForDetail(detail);

    assertThat(result).isEmpty();

  }

  @Test
  public void invalid_mandatory_huooType_portalOrg() {

    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of());

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.required")),
        entry("huooRoles", Set.of("huooRoles.required", "huooRoles.requiresOneHolder"))
    );

  }


  @Test
  public void invalid_huooType_portalOrg_duplicateOrgUnitAlreadyHasRoleOnPwa() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER));

    var orgRole = new PadOrganisationRole();
    var ou = PortalOrganisationTestUtils.generateOrganisationUnit(99, "org", null);

    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setOrganisationUnit(ou);
    orgRole.setRole(HuooRole.OWNER);

    portalOrgRoles = new ArrayList<>(portalOrgRoles);
    portalOrgRoles.add(orgRole);

    var validationView = getValidationView(List.of(portalOrgRoles.get(0)));

    form.setOrganisationUnitId(99);

    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(portalOrgRoles);

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, validationView, authenticatedUserAccount);

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of("organisationUnitId.alreadyUsed"))
    );

  }

  @Test
  public void invalid_huooType_portalOrg_lastHolder() {

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.OWNER));
    form.setOrganisationUnitId(orgUnit.getOuId());

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    assertThat(result).containsOnly(
        entry("huooRoles", Set.of("huooRoles.requiresOneHolder"))
    );

  }


  @Test
  public void unitSelectedIsPartOfUsersOrg_valid() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(ORG_UNIT_1_ID.asInt());
    form.setHuooRoles(Set.of());
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.USER)
    );

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

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
    form.setOrganisationUnitId(ORG_UNIT_1_ID.asInt());
    form.setHuooRoles(Set.of(HuooRole.HOLDER));
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.HOLDER)
    );
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any()))
        .thenReturn(List.of(PortalOrganisationTestUtils.generateOrganisationUnit(2, "name")));

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

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
    form.setOrganisationUnitId(ORG_UNIT_1_ID.asInt());
    form.setHuooRoles(Set.of());
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.HOLDER)
    );
    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(any()))
        .thenReturn(List.of(PortalOrganisationTestUtils.generateOrganisationUnit(2, "name")));

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

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
    form.setOrganisationUnitId(ORG_UNIT_1_ID.asInt());
    form.setHuooRoles(Set.of(HuooRole.USER, HuooRole.OPERATOR, HuooRole.OWNER));
    var application = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    detail.setPwaApplication(application);

    portalOrgRoles = List.of(
        new PadOrganisationRole(HuooRole.USER)
    );

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    assertThat(result).doesNotContainKeys("organisationUnitId");
  }

  @Test
  public void unitSelectedIsPartOfUsersOrg_variationPwa() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(ORG_UNIT_1_ID.asInt());
    form.setHuooRoles(Set.of());
    var application = new PwaApplication(null, PwaApplicationType.HUOO_VARIATION, null);
    detail.setPwaApplication(application);


    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

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
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "");
    padOrgRole1.setOrganisationUnit(orgUnit);
    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(padOrgRole1));

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

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
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "");
    padOrgRole1.setOrganisationUnit(orgUnit);
    when(organisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(padOrgRole1));

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    assertThat(result).doesNotContain(
        entry("huooRoles", Set.of("huooRoles.alreadyUsed"))
    );
  }

  @Test
  public void validate_orgUnitIsInvalid_invalid() {
    detail.getPwaApplication().setApplicationType(PwaApplicationType.INITIAL);
    var portalOrgRole = new PadOrganisationRole();
    portalOrgRole.setType(HuooType.PORTAL_ORG);
    portalOrgRole.setRole(HuooRole.HOLDER);

    orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(ORG_UNIT_1_ID.asInt(), "org", null);
    portalOrgRole.setOrganisationUnit(orgUnit);

    when(organisationRoleService.organisationExistsAndActive(orgUnit.getOuId())).thenReturn(false);

    var form = buildForm();
    form.setHuooRoles(Set.of(HuooRole.HOLDER, HuooRole.OWNER));
    form.setOrganisationUnitId(orgUnit.getOuId());

    var result = ValidatorTestUtils.getFormValidationErrors(
        validator, form, detail, getValidationView(portalOrgRoles), authenticatedUserAccount);

    assertThat(result).containsOnly(
        entry("organisationUnitId", Set.of(FieldValidationErrorCodes.INVALID.errorCode("organisationUnitId")))
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
