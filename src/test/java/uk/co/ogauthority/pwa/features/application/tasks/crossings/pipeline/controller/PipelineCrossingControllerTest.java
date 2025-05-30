package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingOwnerService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.PipelineCrossingFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsSection;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = PipelineCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class PipelineCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private int APP_ID = 100;

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;
  private PwaApplicationEndpointTestBuilder endpointTester;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineCrossingService padPipelineCrossingService;

  @MockBean
  private PipelineCrossingFileService pipelineCrossingFileService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  @MockBean
  private PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;

  @MockBean
  private PipelineCrossingFormValidator pipelineCrossingFormValidator;

  @MockBean
  private PadFileManagementService padFileManagementService;

  @BeforeEach
  void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING);

    when(pwaApplicationDetailService.getTipDetailByAppId(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING
        )
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);
  }

  @Test
  void renderOverview_appTypeSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.PIPELINE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

    verify(padFileManagementService, times(endpointTester.getAllowedTypes().size())).getUploadedFileViews(any(),
        eq(FileDocumentType.PIPELINE_CROSSINGS));
  }

  @Test
  void renderOverview_appStatusSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.PIPELINE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

    verify(padFileManagementService, times(endpointTester.getAllowedStatuses().size())).getUploadedFileViews(any(),
        eq(FileDocumentType.PIPELINE_CROSSINGS));
  }

  @Test
  void renderOverview_appContactRoleSmokeTest() {

    var crossingAgreementsValidationResult = new CrossingAgreementsValidationResult(
        Set.of(CrossingAgreementsSection.PIPELINE_CROSSINGS));
    when(crossingAgreementsService.getValidationResult(any()))
        .thenReturn(crossingAgreementsValidationResult);

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .renderOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

    verify(padFileManagementService, times(endpointTester.getAllowedAppPermissions().size())).getUploadedFileViews(any(),
        eq(FileDocumentType.PIPELINE_CROSSINGS));
  }

  @Test
  void postOverview_appTypeSmokeTest() {

    when(padPipelineCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  void postOverview_appStatusSmokeTest() {

    when(padPipelineCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  void postOverview_appContactRoleSmokeTest() {

    when(padPipelineCrossingService.isComplete(any())).thenReturn(true);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());
  }

}
