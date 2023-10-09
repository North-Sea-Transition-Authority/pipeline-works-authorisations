package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PadPipelineTaskListService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResultTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelinesTaskListController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelinesTaskListControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 1;
  private static final Set<PipelineStatus> allowedPipelineStatuses = Set.of(
      PipelineStatus.IN_SERVICE, PipelineStatus.OUT_OF_USE_ON_SEABED
  );
  private static final Set<PipelineStatus> disallowedPipelineStatuses = Set.of(
      PipelineStatus.RETURNED_TO_SHORE, PipelineStatus.NEVER_LAID
  );

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineTaskListService padPipelineTaskListService;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private PadPipeline padPipeline;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    var pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline = new PadPipeline();
    padPipeline.setId(1);
    padPipeline.setPipeline(pipeline);
    padPipeline.setLength(BigDecimal.ONE);
    padPipeline.setPipelineRef("ref");
    padPipeline.setPipelineType(PipelineType.UNKNOWN);
    padPipeline.setPipelineMaterial(PipelineMaterial.DUPLEX);
    padPipeline.setFromLocation("from");
    padPipeline.setToLocation("to");
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    when(padPipelineService.getById(1)).thenReturn(padPipeline);

    var overview = new PadPipelineOverview(padPipeline, 0L);
    when(padPipelineService.getPipelineOverview(padPipeline))
        .thenReturn(overview);
  }

  @Test
  public void renderPipelinesOverview_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesTaskListController.class)
                .renderPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesTaskListController.class)
                .renderPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesTaskListController.class)
                .renderPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postPipelinesOverview_permissionSmokeTest() {

    when(padPipelineTaskListService.isComplete(any())).thenReturn(true);
    when(padPipelineTaskListService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesTaskListController.class)
                .postPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postPipelinesOverview_appTypeSmokeTest() {

    when(padPipelineTaskListService.isComplete(any())).thenReturn(true);
    when(padPipelineTaskListService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesTaskListController.class)
                .postPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postPipelinesOverview_appStatusSmokeTest() {

    when(padPipelineTaskListService.isComplete(any())).thenReturn(true);
    when(padPipelineTaskListService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesTaskListController.class)
                .postPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postPipelinesOverview_validationFail_pipelinesInvalid() throws Exception {

    when(padPipelineTaskListService.isComplete(pwaApplicationDetail)).thenReturn(false);

    when(padPipelineTaskListService.getValidationResult(pwaApplicationDetail)).thenReturn(
        SummaryScreenValidationResultTestUtils.incompleteResult());

    mockMvc.perform(post(ReverseRouter.route(on(PipelinesTaskListController.class)
        .postPipelinesOverview(pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/pipelines/overview"))
        .andExpect(model().attributeExists("pipelineSummaryValidationResult"));

  }

  @Test
  public void postPipelinesOverview_validationPass() throws Exception {

    when(padPipelineTaskListService.isComplete(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineTaskListService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    mockMvc.perform(post(ReverseRouter.route(on(PipelinesTaskListController.class)
        .postPipelinesOverview(pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

}
