package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.h2.mvstore.DataUtils;
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
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadField;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaFieldForm;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PadPwaFieldsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PadPwaFieldsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static int APP_ID = 10;
  private static int APP_DETAIL_ID = 100;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadFieldService padFieldService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadField padField;
  private DevukField devukField;

  private AuthenticatedUserAccount user;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @Before
  public void setUp() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL, APP_ID, APP_DETAIL_ID
    );
    devukField = new DevukField(1, "abc", 500);
    padField = new PadField();
    padField.setId(1);
    padField.setDevukField(devukField);

    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(padFieldService.getActiveFieldsForApplicationDetail(any())).thenReturn(List.of(padField));

    when(devukFieldService.getByStatusCodes(List.of(500, 600, 700))).thenReturn(List.of(devukField));

    doCallRealMethod().when(applicationBreadcrumbService).fromTaskList(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(PwaApplicationType.values())
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

  }

  @Test
  public void renderFields_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaFieldsController.class)
                .renderFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderFields_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaFieldsController.class)
                .renderFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderFields_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaFieldsController.class)
                .renderFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }


  @Test
  public void renderFields() throws Exception {

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(PadPwaFieldsController.class)
        .renderFields(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("breadcrumbMap", "currentPage", "backUrl"))
        .andExpect(view().name("pwaApplication/shared/fieldInformation/fieldInformation"))
        .andReturn()
        .getModelAndView();
    var fields = (List<PadField>) modelAndView.getModel().get("fields");
    var fieldMap = (Map<String, String>) modelAndView.getModel().get("fieldMap");
    assertThat(fields).containsExactly(padField);
    assertThat(fieldMap).containsExactly(new DataUtils.MapEntry<>(padField.getDevukField().getFieldId().toString(), padField.getDevukField().getFieldName()));
  }

  @Test
  public void postFields_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padFieldService, new PwaFieldForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaFieldsController.class)
                .postFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postFields_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padFieldService, new PwaFieldForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaFieldsController.class)
                .postFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postFields_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padFieldService, new PwaFieldForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaFieldsController.class)
                .postFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postFields_validationFailed() throws Exception {

    ControllerTestUtils.failValidationWhenPost(padFieldService, new PwaFieldForm(), ValidationType.FULL);

    mockMvc.perform(post(ReverseRouter.route(on(PadPwaFieldsController.class)
        .postFields(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .params(ControllerTestUtils.fullValidationPostParams())
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/fieldInformation/fieldInformation"))
    .andReturn().getModelAndView();


    verify(padFieldService, times(0)).updateFieldInformation(any(), any());

  }


  @Test
  public void postFields_valid() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padFieldService, new PwaFieldForm(), ValidationType.PARTIAL);

    mockMvc.perform(post(ReverseRouter.route(on(PadPwaFieldsController.class)
        .postFields(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null, null, null, null)))
        .with(authenticatedUserAndSession(user))
        .params(ControllerTestUtils.partialValidationPostParams())
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(padFieldService, times(1)).getActiveFieldsForApplicationDetail(any());
    verify(padFieldService, times(1)).updateFieldInformation(any(), any());

  }

}