package uk.co.ogauthority.pwa.features.application.tasks.othertechprops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@WebMvcTest(controllers = PipelineOtherPropertiesController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class PipelineOtherPropertiesControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;


  @BeforeEach
  void setUp() {
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
  }


  @Test
  void renderAddPipelineOtherProperties_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .renderAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(),null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderAddPipelineOtherProperties_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .renderAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddPipelineOtherProperties_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .renderAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }


  @Test
  void postAddPipelineOtherProperties_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padPipelineOtherPropertiesService, new PipelineOtherPropertiesForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .postAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  void postAddPipelineOtherProperties_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padPipelineOtherPropertiesService, new PipelineOtherPropertiesForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .postAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postAddPipelineOtherProperties_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padPipelineOtherPropertiesService, new PipelineOtherPropertiesForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .postAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddPipelineOtherProperties_failValidation() {
    ControllerTestUtils.failValidationWhenPost(padPipelineOtherPropertiesService, new PipelineOtherPropertiesForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .postAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postAddPipelineOtherProperties_minMaxFailValidation() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new PipelineOtherPropertiesForm(), "form");
    bindingResult.addError(new FieldError("form", "propertyDataFormMap[WAX_CONTENT].minMaxInput.minValue", "fake"));
    bindingResult.addError(new FieldError("form", "propertyDataFormMap[WAX_CONTENT].minMaxInput.maxValue", "fake"));
    when(padPipelineOtherPropertiesService.validate(any(), any(), any(), any())).thenReturn(bindingResult);


    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PipelineOtherPropertiesController.class)
                .postAddPipelineOtherProperties(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));


    ArrayList<ErrorItem> errorList = (ArrayList<ErrorItem>) endpointTester.performModelGeneration().get("errorList");
    assertThat(errorList.get(0).getFieldName()).isEqualTo("propertyDataFormMap[WAX_CONTENT].minMaxInput.minValue");
    assertThat(errorList.get(1).getFieldName()).isEqualTo("propertyDataFormMap[WAX_CONTENT].minMaxInput.maxValue");
  }




}
