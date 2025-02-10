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
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadLinkedArea;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PwaAreaForm;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
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
@WebMvcTest(controllers = PadPwaAreaController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PadPwaAreaControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static int APP_ID = 10;
  private static int APP_DETAIL_ID = 100;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadAreaService padAreaService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadLinkedArea padLinkedArea;
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
    padLinkedArea = new PadLinkedArea();
    padLinkedArea.setId(1);
    padLinkedArea.setDevukField(devukField);

    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    when(padAreaService.getActiveFieldsForApplicationDetail(any())).thenReturn(List.of(padLinkedArea));

    when(devukFieldService.getAllFields()).thenReturn(List.of(devukField));

    doCallRealMethod().when(applicationBreadcrumbService).fromTaskList(any(), any(), any());
    // set default checks for entire controller
    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(PwaApplicationType.values())
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

  }

  @Test
  public void renderFields_petroleumSmokeTest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PadPwaAreaController.class)
            .renderFields(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/areaInformation/fieldInformation"));
  }

  @Test
  public void renderFields_hydrogenSmokeTest() throws Exception {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, PwaResourceType.HYDROGEN, APP_ID, APP_DETAIL_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    mockMvc.perform(get(ReverseRouter.route(on(PadPwaAreaController.class)
            .renderFields(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/areaInformation/fieldInformation"));
  }

  @Test
  public void renderFields_ccusSmokeTest() throws Exception {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, PwaResourceType.CCUS, APP_ID, APP_DETAIL_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    mockMvc.perform(get(ReverseRouter.route(on(PadPwaAreaController.class)
            .renderFields(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/areaInformation/storageInformation"));
  }

  @Test
  public void renderFields_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaAreaController.class)
                .renderFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderFields_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaAreaController.class)
                .renderFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderFields_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaAreaController.class)
                .renderFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }


  @Test
  public void renderFields() throws Exception {

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(PadPwaAreaController.class)
        .renderFields(PwaApplicationType.INITIAL, APP_ID, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("breadcrumbMap", "currentPage", "backUrl"))
        .andExpect(view().name("pwaApplication/shared/areaInformation/fieldInformation"))
        .andReturn()
        .getModelAndView();
    var fields = (List<PadLinkedArea>) modelAndView.getModel().get("fields");
    var fieldMap = (Map<String, String>) modelAndView.getModel().get("fieldMap");
    assertThat(fields).containsExactly(padLinkedArea);
    assertThat(fieldMap).containsExactly(
        Map.entry(padLinkedArea.getDevukField().getFieldId().toString(), padLinkedArea.getDevukField().getFieldName()));
  }

  @Test
  public void postFields_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padAreaService, new PwaAreaForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaAreaController.class)
                .postFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postFields_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padAreaService, new PwaAreaForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaAreaController.class)
                .postFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postFields_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(padAreaService, new PwaAreaForm(), ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PadPwaAreaController.class)
                .postFields(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postFields_validationFailed() throws Exception {

    ControllerTestUtils.failValidationWhenPost(padAreaService, new PwaAreaForm(), ValidationType.FULL);

    mockMvc.perform(post(ReverseRouter.route(on(PadPwaAreaController.class)
        .postFields(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null, null, null, null)))
        .with(user(user))
        .params(ControllerTestUtils.fullValidationPostParams())
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/areaInformation/fieldInformation"))
    .andReturn().getModelAndView();


    verify(padAreaService, times(0)).updateFieldInformation(any(), any());

  }


  @Test
  public void postFields_valid() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padAreaService, new PwaAreaForm(), ValidationType.PARTIAL);

    mockMvc.perform(post(ReverseRouter.route(on(PadPwaAreaController.class)
        .postFields(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null, null, null, null)))
        .with(user(user))
        .params(ControllerTestUtils.partialValidationPostParams())
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(padAreaService, times(1)).getActiveFieldsForApplicationDetail(any());
    verify(padAreaService, times(1)).updateFieldInformation(any(), any());

  }

}
