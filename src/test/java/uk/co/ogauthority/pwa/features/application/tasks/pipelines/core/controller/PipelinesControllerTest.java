package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.List;
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
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineRemovalService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelinesController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelinesControllerTest extends PwaApplicationContextAbstractControllerTest {

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
  private PipelineHeaderFormValidator validator;

  @MockBean
  private PipelineRemovalService pipelineRemovalService;

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
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
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
  public void renderAddPipeline_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddPipeline_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddPipeline_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postAddPipeline_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddPipeline_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddPipeline_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddPipeline_validationFailed() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromLocation"));

    mockMvc.perform(post(ReverseRouter.route(on(PipelinesController.class)
        .postAddPipeline(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(),
            null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/pipelines/addEditPipeline"))
        .andExpect(model().attributeHasErrors("form"));

    verify(padPipelineService, times(0)).addPipeline(eq(pwaApplicationDetail), any());

  }

  @Test
  public void postAddPipeline_valid() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(PipelinesController.class)
        .postAddPipeline(pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(),
            null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(padPipelineService, times(1)).addPipeline(eq(pwaApplicationDetail), any());

  }

  @Test
  public void renderRemovePipeline_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemovePipeline_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemovePipeline_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postRemovePipeline_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

    verify(pipelineRemovalService, times(endpointTester.getAllowedAppPermissions().size())).removePipeline(padPipeline);

  }

  @Test
  public void postRemovePipeline_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

    verify(pipelineRemovalService, times(endpointTester.getAllowedTypes().size())).removePipeline(padPipeline);

  }

  @Test
  public void postRemovePipeline_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

    verify(pipelineRemovalService, times(endpointTester.getAllowedStatuses().size())).removePipeline(padPipeline);

  }

  @Test
  public void renderEditPipeline_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
        });

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditPipeline_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
        });

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditPipeline_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
        });

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderEditPipeline_pipelineStatusAllowed() {
    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            padPipeline.setPwaApplicationDetail(applicationDetail);
            padPipeline.setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelinesController.class)
                .renderEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
    });
  }

  @Test
  public void renderEditPipeline_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.GET)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            padPipeline.setPwaApplicationDetail(applicationDetail);
            padPipeline.setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelinesController.class)
                .renderEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  public void postEditPipeline_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
        });

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

    verify(padPipelineService, times(endpointTester.getAllowedAppPermissions().size())).updatePipeline(eq(padPipeline), any());

  }

  @Test
  public void postEditPipeline_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
        });

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

    verify(padPipelineService, times(endpointTester.getAllowedTypes().size())).updatePipeline(eq(padPipeline), any());

  }

  @Test
  public void postEditPipeline_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
        });

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

    verify(padPipelineService, times(endpointTester.getAllowedStatuses().size())).updatePipeline(eq(padPipeline), any());

  }

  @Test
  public void postEditPipeline_pipelineStatusAllowed() {

    allowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            padPipeline.setPwaApplicationDetail(applicationDetail);
            padPipeline.setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelinesController.class)
                .postEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
    });
  }

  @Test
  public void postEditPipeline_pipelineStatusNotAllowed() {
    disallowedPipelineStatuses.forEach(pipelineStatus -> {
      endpointTester.setRequestMethod(HttpMethod.POST)
          .setEndpointUrlProducer((applicationDetail, type) -> {
            padPipeline.setPwaApplicationDetail(applicationDetail);
            padPipeline.setPipelineStatus(pipelineStatus);
            return ReverseRouter.route(on(PipelinesController.class)
                .postEditPipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null, null, null));
          });

      endpointTester.performAppTypeChecks(status().is(403), status().isForbidden());
    });
  }

}
