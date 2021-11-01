package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.math.BigDecimal;
import java.util.EnumSet;
import org.apache.commons.lang3.Range;
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
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.RegulatorPipelineNumberTaskService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SetPipelineNumberController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class SetPipelineNumberControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 20;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;
  private static final int PAD_PIPELINE_ID = 20;


  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService;

  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private PadPipeline padPipeline;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService,
        pwaApplicationDetailService, padPipelineService)
        .setAllowedPermissions(PwaApplicationPermission.SET_PIPELINE_REFERENCE)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE)
        .setAllowedTypes(PwaApplicationType.values());


    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(regulatorPipelineNumberTaskService.pipelineTaskAccessible(any(), any()))
        .thenReturn(true);

    var pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline = new PadPipeline();
    padPipeline.setId(PAD_PIPELINE_ID);
    padPipeline.setPipeline(pipeline);
    padPipeline.setLength(BigDecimal.ONE);
    padPipeline.setPipelineRef("ref");
    padPipeline.setPipelineType(PipelineType.UNKNOWN);
    padPipeline.setPipelineMaterial(PipelineMaterial.DUPLEX);
    padPipeline.setFromLocation("from");
    padPipeline.setToLocation("to");
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    padPipeline.setPwaApplicationDetail(pwaApplicationDetail);
    when(padPipelineService.getById(PAD_PIPELINE_ID)).thenReturn(padPipeline);

    var overview = new PadPipelineOverview(padPipeline, 0L);
    when(padPipelineService.getById(PAD_PIPELINE_ID)).thenReturn(padPipeline);
    when(padPipelineService.getPipelineOverview(padPipeline))
        .thenReturn(overview);

    when(regulatorPipelineNumberTaskService.getPermittedPipelineNumberRange()).thenReturn(Range.between(1000, 2000));
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(2);
      errors.rejectValue("pipelineNumber", FieldValidationErrorCodes.REQUIRED.errorCode("pipelineNumber"), "fake");
      return errors;
    }).when(regulatorPipelineNumberTaskService).validateForm(any(), any(), any());

  }

  @Test
  public void renderSetPipelineReference_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineNumberController.class)
                .renderSetPipelineNumber(type, applicationDetail.getMasterPwaApplicationId(),
                    PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSetPipelineReference_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineNumberController.class)
                .renderSetPipelineNumber(type, applicationDetail.getMasterPwaApplicationId(),
                    PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSetPipelineReference_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineNumberController.class)
                .renderSetPipelineNumber(type, applicationDetail.getMasterPwaApplicationId(),
                    PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void setPipelineReference_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineNumberController.class)
                .setPipelineReference(type, applicationDetail.getMasterPwaApplicationId(),
                    PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void setPipelineReference_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineNumberController.class)
                .setPipelineReference(type, applicationDetail.getMasterPwaApplicationId(),
                    PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void setPipelineReference_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineNumberController.class)
                .setPipelineReference(type, applicationDetail.getMasterPwaApplicationId(),
                    PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void setPipelineReference_failValidation() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(SetPipelineNumberController.class)
            .setPipelineReference(APP_TYPE, APP_ID, PAD_PIPELINE_ID, null, null, null)
        ))
            .with(authenticatedUserAndSession(user))
            .with(csrf())

    ).andExpect(status().is2xxSuccessful())
        .andExpect(model().hasErrors());

    verify(regulatorPipelineNumberTaskService, times(0)).setPipelineNumber(any(), any());
  }


  @Test
  public void setPipelineReference_passValidation() throws Exception {
    doAnswer(invocation -> invocation).when(regulatorPipelineNumberTaskService).validateForm(any(), any(), any());

    mockMvc.perform(
        post(ReverseRouter.route(on(SetPipelineNumberController.class)
            .setPipelineReference(APP_TYPE, APP_ID, PAD_PIPELINE_ID, null, null, null)
        ))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .param("pipelineNumber", "PL1000")

    ).andExpect(status().is3xxRedirection());

    verify(regulatorPipelineNumberTaskService, times(1)).setPipelineNumber(padPipeline, "PL1000");
  }
}
