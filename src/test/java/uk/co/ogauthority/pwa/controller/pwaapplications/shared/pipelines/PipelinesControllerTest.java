package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineHeaderFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineRemovalService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResultTestUtils;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelinesController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelinesControllerTest extends PwaApplicationContextAbstractControllerTest {

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
  private int APP_ID = 1;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));

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
  public void renderAddPipeline_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

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
  public void postAddPipeline_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postAddPipeline(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

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

    verifyNoInteractions(padPipelineService);

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
  public void renderPipelinesOverview_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPipelinesOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .renderPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postPipelinesOverview_contactSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postPipelinesOverview_appTypeSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postPipelinesOverview_appStatusSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelinesController.class)
                .postPipelinesOverview(applicationDetail.getMasterPwaApplicationId(), type, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postPipelinesOverview_validationFail_pipelinesInvalid() throws Exception {

    when(padPipelineService.isComplete(pwaApplicationDetail)).thenReturn(false);

    when(padPipelineService.getValidationResult(pwaApplicationDetail)).thenReturn(
        SummaryScreenValidationResultTestUtils.incompleteResult());

    mockMvc.perform(post(ReverseRouter.route(on(PipelinesController.class)
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

    when(padPipelineService.isComplete(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    mockMvc.perform(post(ReverseRouter.route(on(PipelinesController.class)
        .postPipelinesOverview(pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(), null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void renderRemovePipeline_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .renderRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());

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
  public void postRemovePipeline_contactSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

    verify(pipelineRemovalService, times(endpointTester.getContactRoles().size())).removePipeline(padPipeline);

  }

  @Test
  public void postRemovePipeline_appTypeSmokeTest() {

    when(padPipelineService.isComplete(any())).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

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

    when(padPipelineService.isComplete(any())).thenReturn(true);
    when(padPipelineService.getValidationResult(any())).thenReturn(SummaryScreenValidationResultTestUtils.completeResult());

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) -> {
          padPipeline.setPwaApplicationDetail(applicationDetail);
          return ReverseRouter.route(on(PipelinesController.class)
              .postRemovePipeline(applicationDetail.getMasterPwaApplicationId(), type, 1, null));
        });

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

    verify(pipelineRemovalService, times(endpointTester.getAllowedStatuses().size())).removePipeline(padPipeline);

  }

}
