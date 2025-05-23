package uk.co.ogauthority.pwa.features.application.tasks.crossings.types.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.CrossingTypesFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.CrossingTypesService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;

@WebMvcTest(controllers = CrossingTypesController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class CrossingTypesControllerTest extends PwaApplicationContextAbstractControllerTest {

  private int APP_ID = 100;
  private PwaApplicationEndpointTestBuilder endpointTester;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private CrossingTypesFormValidator crossingTypesFormValidator;

  @MockBean
  private CrossingTypesService crossingTypesService;

  @BeforeEach
  void setUp() {
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
  void renderForm_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CrossingTypesController.class)
                .renderForm(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void renderForm_appStatusSmokeTest() throws Exception {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CrossingTypesController.class)
                .renderForm(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
    endpointTester.performModelGeneration().containsKey("resourceType");
  }

  @Test
  void renderForm_appContactRoleSmokeTest() throws Exception {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CrossingTypesController.class)
                .renderForm(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
    endpointTester.performModelGeneration().containsKey("resourceType");
  }

  @Test
  void postForm_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CrossingTypesController.class)
                .postForm(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)))
    .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  void postForm_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CrossingTypesController.class)
                .postForm(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  void postForm_appContactRoleSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(CrossingTypesController.class)
                .postForm(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());
  }
}
