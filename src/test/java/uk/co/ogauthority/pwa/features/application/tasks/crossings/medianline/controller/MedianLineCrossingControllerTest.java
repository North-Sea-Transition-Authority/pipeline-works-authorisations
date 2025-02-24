package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MedianLineAgreementView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = MedianLineCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class MedianLineCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {
  private static final int APP_ID = 100;
  private static final PwaApplicationType TYPE = PwaApplicationType.INITIAL;

  @MockBean
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private MedianLineCrossingFileService medianLineCrossingFileService;

  @Mock
  private MedianLineAgreementView medianLineAgreementView;

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;

  private PwaApplicationEndpointTestBuilder endpointTester;


  @BeforeEach
  void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(TYPE, APP_ID);

    when(medianLineAgreementView.getSortedFileViews()).thenReturn(List.of());
    when(padMedianLineAgreementService.getMedianLineCrossingView(any())).thenReturn(medianLineAgreementView);

    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING);

    when(pwaApplicationDetailService.getTipDetailByAppId(APP_ID)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_ACCESS));

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
  void renderMedianLineForm_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(MedianLineCrossingController.class).renderMedianLineForm(invalidAppType, APP_ID, null, null)))
                    .with(user(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  void renderMedianLineForm_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(MedianLineCrossingController.class).renderMedianLineForm(PwaApplicationType.INITIAL, APP_ID, null,
                null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  void renderMedianLineOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .renderMedianLineOverview(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderMedianLineOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .renderMedianLineOverview(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void renderMedianLineOverview_appContactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .renderMedianLineOverview(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void postOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());
  }

  @Test
  void postOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void postOverview_appContactRoleSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postOverview(type, applicationDetail.getMasterPwaApplicationId(), null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void postOverview_notValid() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(MedianLineCrossingController.class)
        .postOverview(TYPE, APP_ID, null)))
        .with(user(user))
        .param(ValidationType.FULL.getButtonText(), "")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("errorMessage"));

    verify(padMedianLineAgreementService, times(1)).getMedianLineCrossingView(pwaApplicationDetail);
  }

  @Test
  void postOverview_valid() throws Exception {
    when(padMedianLineAgreementService.isMedianLineAgreementFormComplete(pwaApplicationDetail)).thenReturn(true);
    when(medianLineCrossingFileService.isComplete(pwaApplicationDetail)).thenReturn(true);

    mockMvc.perform(post(ReverseRouter.route(on(MedianLineCrossingController.class)
        .postOverview(TYPE, APP_ID, null)))
        .with(user(user))
        .param(ValidationType.FULL.getButtonText(), "")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(padMedianLineAgreementService, times(0)).getMedianLineCrossingView(any());
  }

  @Test
  void postAddContinueMedianLine_appTypeSmokeTest_complete() {

    var form = new MedianLineAgreementsForm();
    ControllerTestUtils.passValidationWhenPost(padMedianLineAgreementService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postEditMedianLine(type, applicationDetail.getMasterPwaApplicationId(),
                    null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  void postAddContinueMedianLine_appStatusSmokeTest() {
    var form = new MedianLineAgreementsForm();
    ControllerTestUtils.passValidationWhenPost(padMedianLineAgreementService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postEditMedianLine(type, applicationDetail.getMasterPwaApplicationId(),
                    null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());
  }

  @Test
  void postAddContinueMedianLine_appContactRoleSmokeTest() {
    var form = new MedianLineAgreementsForm();
    ControllerTestUtils.passValidationWhenPost(padMedianLineAgreementService, form, ValidationType.FULL);

    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(MedianLineCrossingController.class)
                .postEditMedianLine(type, applicationDetail.getMasterPwaApplicationId(),
                    null, null, null, null)))
        .addRequestParam(ValidationType.FULL.getButtonText(), "");

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());
  }

}
