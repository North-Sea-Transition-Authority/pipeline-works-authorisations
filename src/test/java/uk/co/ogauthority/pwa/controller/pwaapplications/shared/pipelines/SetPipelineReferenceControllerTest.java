package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.math.BigDecimal;
import java.util.EnumSet;
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
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist.RegulatorPipelineReferenceTaskService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SetPipelineReferenceController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class SetPipelineReferenceControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 20;
  private static final int PAD_PIPELINE_ID = 20;


  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private RegulatorPipelineReferenceTaskService regulatorPipelineReferenceTaskService;
  
  private PwaApplicationEndpointTestBuilder endpointTester;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private PadPipeline padPipeline;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService, padPipelineService)
        .setAllowedPermissions(PwaApplicationPermission.SET_PIPELINE_REFERENCE)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE)
        .setAllowedTypes(PwaApplicationType.values());


    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(
        pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(regulatorPipelineReferenceTaskService.pipelineTaskAccessible(any(), any()))
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
  }

  @Test
  public void renderSetPipelineReference_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineReferenceController.class)
                .renderSetPipelineReference(type, applicationDetail.getMasterPwaApplicationId(), PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSetPipelineReference_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineReferenceController.class)
                .renderSetPipelineReference(type, applicationDetail.getMasterPwaApplicationId(), PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderSetPipelineReference_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(SetPipelineReferenceController.class)
                .renderSetPipelineReference(type, applicationDetail.getMasterPwaApplicationId(), PwaApplicationEndpointTestBuilder.PAD_PIPELINE_ID, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

}
