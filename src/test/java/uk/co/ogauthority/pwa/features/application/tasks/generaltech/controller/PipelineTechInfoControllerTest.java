package uk.co.ogauthority.pwa.features.application.tasks.generaltech.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineTechInfoController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelineTechInfoControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineTechInfoService padPipelineTechInfoService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaApplicationContext pwaApplicationContext;


  @Before
  public void setUp() {
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    var user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    pwaApplicationDetail.getPwaApplication().setResourceType(PwaResourceType.PETROLEUM);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    pwaApplicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Collections.emptySet());
  }




  @Test
  public void renderAddPipelineTechInfo_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .renderAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(),pwaApplicationContext, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderAddPipelineTechInfo_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .renderAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(), pwaApplicationContext, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddPipelineTechInfo_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .renderAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(), pwaApplicationContext, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }



  @Test
  public void postAddPipelineTechInfo_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padPipelineTechInfoService, new PipelineTechInfoForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .postAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(), pwaApplicationContext, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  public void postAddPipelineTechInfo_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padPipelineTechInfoService, new PipelineTechInfoForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .postAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(), pwaApplicationContext, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddPipelineTechInfo_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padPipelineTechInfoService, new PipelineTechInfoForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .postAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(), pwaApplicationContext, null, null, ValidationType.FULL)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddPipelineTechInfo_failValidation() {
    ControllerTestUtils.failValidationWhenPost(padPipelineTechInfoService, new PipelineTechInfoForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineTechInfoController.class)
                .postAddPipelineTechInfo(type, applicationDetail.getMasterPwaApplicationId(), pwaApplicationContext, null, null, ValidationType.FULL)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }




}
