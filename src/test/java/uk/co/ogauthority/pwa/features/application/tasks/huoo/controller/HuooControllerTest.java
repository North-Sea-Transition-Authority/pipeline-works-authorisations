package uk.co.ogauthority.pwa.features.application.tasks.huoo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.HuooSummaryValidationResult;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.HuooSummaryValidationResultTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooSummaryView;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooSummaryViewService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooSummaryViewTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooValidationService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(
    controllers = HuooController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
class HuooControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 100;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadHuooValidationService padHuooValidationService;

  @MockBean
  private PadHuooSummaryViewService padHuooSummaryViewService;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  private HuooSummaryValidationResult validationResult;

  private PadHuooSummaryView padHuooSummaryView;

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
            PwaApplicationType.HUOO_VARIATION,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(pwaApplicationDetail);

    validationResult = HuooSummaryValidationResultTestUtil.validResult();
    when(padHuooValidationService.getHuooSummaryValidationResult(any())).thenReturn(validationResult);

    padHuooSummaryView = PadHuooSummaryViewTestUtil.getEmptySummaryView();
    when(padHuooSummaryViewService.getPadHuooSummaryView(any())).thenReturn(padHuooSummaryView);

  }

  @Test
  void renderHuooSummary_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(HuooController.class)
                .renderHuooSummary(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderHuooSummary_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(HuooController.class)
                .renderHuooSummary(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderHuooSummary_contactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(HuooController.class)
                .renderHuooSummary(
                    type,
                    applicationDetail.getMasterPwaApplicationId(),
                    null,
                    null)
            )
        );

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postHuooSummary_Invalid() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(Set.of(PwaApplicationPermission.EDIT));

    validationResult = HuooSummaryValidationResultTestUtil.invalidResult();
    when(padHuooValidationService.getHuooSummaryValidationResult(any())).thenReturn(validationResult);

    mockMvc.perform(post(ReverseRouter.route(on(HuooController.class)
        .postHuooSummary(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("errorMessage"));

  }

  @Test
  void postHuooSummary_Valid() throws Exception {

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(Set.of(PwaApplicationPermission.EDIT));


    mockMvc.perform(post(ReverseRouter.route(on(HuooController.class)
        .postHuooSummary(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }


}
