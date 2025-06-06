package uk.co.ogauthority.pwa.features.application.tasks.huoo.controller;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.AddHuooValidator;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.EditHuooValidator;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(
    controllers = AddHuooController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
class AddHuooControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 100;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadOrganisationRoleService padOrganisationRoleService;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private AddHuooValidator addHuooValidator;

  @MockBean
  private EditHuooValidator editHuooValidator;

  private PwaApplicationDetail pwaApplicationDetail;
  private final int ORG_ROLE_ID = 1;
  private final int ORG_UNIT_ID = 2;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(
      PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  @BeforeEach
  void setup() {

    doCallRealMethod().when(applicationBreadcrumbService).fromWorkArea(any(), any());

    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION,
            PwaApplicationType.HUOO_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);

    var portalOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(ORG_UNIT_ID, "Organisation Unit");
    when(portalOrganisationsAccessor.getOrganisationUnitById(anyInt())).thenReturn(Optional.of(portalOrgUnit));

    var padOrganisationRole = new PadOrganisationRole();
    padOrganisationRole.setAgreement(TreatyAgreement.ANY_TREATY_COUNTRY);
    when(padOrganisationRoleService.getOrganisationRole(pwaApplicationDetail, ORG_ROLE_ID)).thenReturn(padOrganisationRole);
  }

  @Test
  void renderAddHuoo_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderAddHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddHuoo_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderAddHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderAddHuoo_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderAddHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddHuoo_modelContentsAsExpected() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    var orgRole = new PadOrganisationRole();
    when(padOrganisationRoleService.getOrganisationRole(pwaApplicationDetail, 1)).thenReturn(orgRole);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(AddHuooController.class)
            .renderAddHuoo(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat((Map<String, String>) modelAndView.getModel().get("huooTypes")).containsExactly(
        entry(HuooType.PORTAL_ORG.name(), HuooType.PORTAL_ORG.getDisplayText()),
        entry(HuooType.TREATY_AGREEMENT.name(), HuooType.TREATY_AGREEMENT.getDisplayText())
    );
  }

  @Test
  void postAddHuoo_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .postAddHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void renderEditOrgHuoo_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderEditOrgHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    ORG_UNIT_ID,
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderEditOrgHuoo_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderEditOrgHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    ORG_UNIT_ID,
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderEditOrgHuoo_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderEditOrgHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    ORG_UNIT_ID,
                    null,
                    null,
                    null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postEditOrgHuoo_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .postEditOrgHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    ORG_UNIT_ID,
                    null,
                    null,
                    null,
                    user)
            )
        );

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditOrgHuoo_notIncludingTreatyFields() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    MultiValueMap parameters = new LinkedMultiValueMap<String, String>() {{
      add("organisationUnitId", "2");
      add("huooRoles", HuooRole.USER.name());
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(AddHuooController.class)
            .postEditOrgHuoo(PwaApplicationType.INITIAL, APP_ID, ORG_UNIT_ID, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(parameters)
    ).andExpect(status().is3xxRedirection());

  }

  @Test
  void renderRemoveOrgHuoo_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderRemoveOrgHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    ORG_UNIT_ID,
                    null
                    )
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderRemoveTreatyHuoo_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(AddHuooController.class)
                .renderRemoveTreatyHuoo(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    ORG_UNIT_ID,
                    null
                )
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderRemoveOrgHuoo() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(AddHuooController.class)
            .renderRemoveOrgHuoo(PwaApplicationType.INITIAL, APP_ID, ORG_UNIT_ID, null)))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat(modelAndView.getModel().get("huooName")).isEqualTo("Organisation Unit");

  }

  @Test
  void renderRemoveTreatyHuoo() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    var orgRole = new PadOrganisationRole();
    when(padOrganisationRoleService.getOrganisationRole(pwaApplicationDetail, 1)).thenReturn(orgRole);

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(AddHuooController.class)
            .renderRemoveTreatyHuoo(PwaApplicationType.INITIAL, APP_ID, ORG_UNIT_ID, null)))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk())
        .andReturn().getModelAndView();

    assertThat(modelAndView.getModel().get("huooName")).isEqualTo("Treaty agreement");

  }

  @Test
  void postRemoveTreatyHuoo() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    mockMvc.perform(
        post(ReverseRouter.route(on(AddHuooController.class)
            .postRemoveTreatyHuoo(PwaApplicationType.INITIAL, APP_ID, ORG_ROLE_ID, null, null)))
            .with(user(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());

  }

  @Test
  void postRemoveOrgHuoo() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    MultiValueMap parameters = new LinkedMultiValueMap<String, String>() {{
      add("organisationUnitId", "2");
      add("huooRoles", HuooRole.USER.name());
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(AddHuooController.class)
            .postRemoveOrgHuoo(PwaApplicationType.INITIAL, APP_ID, ORG_UNIT_ID, null, null)))
            .with(user(user))
            .with(csrf())
            .params(parameters)
    ).andExpect(status().is3xxRedirection());

  }




}
