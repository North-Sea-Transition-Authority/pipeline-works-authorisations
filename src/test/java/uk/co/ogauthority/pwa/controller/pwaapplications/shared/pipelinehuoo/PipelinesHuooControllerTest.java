package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PipelineHuooScreenValidationResultFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineAndOrgRoleGroupViewsByRole;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineAndOrgRoleGroupViewsByRoleTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleSummaryViewTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooRoleValidationResultTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.PipelineHuooValidationResult;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelinesHuooController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelinesHuooControllerTest extends PwaApplicationContextAbstractControllerTest {

  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;
  private final int APP_ID = 10;
  private final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockBean
  private PipelineHuooScreenValidationResultFactory pipelineHuooScreenValidationResultFactory;


  private WebUserAccount wua = new WebUserAccount(1);
  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, EnumSet.allOf(PwaUserPrivilege.class));

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;

  private PipelineAndOrgRoleGroupViewsByRole pipelineAndOrgRoleGroupViewsByRole;

  @Mock
  private PipelineHuooValidationResult validationResult;

  private SummaryScreenValidationResult summaryScreenValidationResult;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID);

    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.HUOO_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pipelineAndOrgRoleGroupViewsByRole = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.HOLDER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    when(padPipelinesHuooService.getPadPipelinesHuooSummaryView(any())).thenReturn(pipelineAndOrgRoleGroupViewsByRole);

    doCallRealMethod().when(applicationBreadcrumbService).fromTaskList(any(), any(), any());

    //return fail validation by default

    when(validationResult.getValidationResults()).thenReturn(
        Map.of(
            HuooRole.HOLDER, PipelineHuooRoleValidationResultTestUtil.invalidResultAsUnassigned("error1", "error2"),
            HuooRole.USER, PipelineHuooRoleValidationResultTestUtil.validResult(),
            HuooRole.OPERATOR, PipelineHuooRoleValidationResultTestUtil.validResult(),
            HuooRole.OWNER, PipelineHuooRoleValidationResultTestUtil.validResult()
        )
    );
    when(validationResult.isValid()).thenReturn(false);
    when(padPipelinesHuooService.generatePipelineHuooValidationResult(any(), any()))
        .thenReturn(validationResult);

    summaryScreenValidationResult = new SummaryScreenValidationResult(
        Map.of(),
        "something",
        "something",
        validationResult.isValid(),
        "something"
    );

    when(pipelineHuooScreenValidationResultFactory.createFromValidationResult(any()))
        .thenReturn(summaryScreenValidationResult);

  }

  @Test
  public void renderSummary_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSummary_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderSummary_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSummary_modelCheck_andServiceInteractions_whenFail() throws Exception {

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(PipelinesHuooController.class)
        .renderSummary(APP_TYPE, APP_ID, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    verify(padPipelinesHuooService, times(1)).getPadPipelinesHuooSummaryView(pwaApplicationDetail);
  }

  @Test
  public void postSummary_smokeCheckRolesAccess() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(PipelinesHuooController.class).postSummary(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), null
            ))));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void postSummary_smokeCheckAppStatus() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(PipelinesHuooController.class).postSummary(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), null
            ))));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postSummary_smokeCheckAppType() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer(((pwaApplicationDetail, pwaApplicationType) ->
            ReverseRouter.route(on(PipelinesHuooController.class).postSummary(
                pwaApplicationType, pwaApplicationDetail.getMasterPwaApplicationId(), null
            ))));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void postSummary_whenFailValidation() throws Exception {

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(PipelinesHuooController.class)
        .postSummary(APP_TYPE, APP_ID, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().isOk())
        .andReturn().getModelAndView();

    verify(padPipelinesHuooService, times(1)).getPadPipelinesHuooSummaryView(pwaApplicationDetail);
    verify(pipelineHuooScreenValidationResultFactory, times(1)).createFromValidationResult(any());
  }

  @Test
  public void postSummary_whenPassValidation() throws Exception {

    when(validationResult.isValid()).thenReturn(true);

    var modelAndView = mockMvc.perform(post(ReverseRouter.route(on(PipelinesHuooController.class)
        .postSummary(APP_TYPE, APP_ID, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf())
    )
        .andExpect(status().is3xxRedirection())
        .andReturn().getModelAndView();

    verify(padPipelinesHuooService, times(1)).getPadPipelinesHuooSummaryView(pwaApplicationDetail);
    verify(pipelineHuooScreenValidationResultFactory, times(0)).createFromValidationResult(any());
  }
}