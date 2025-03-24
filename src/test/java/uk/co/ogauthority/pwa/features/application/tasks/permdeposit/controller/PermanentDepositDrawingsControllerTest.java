package uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingView;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsDrawingValidator;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementControllerTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResultTestUtils;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@WebMvcTest(controllers = PermanentDepositDrawingsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class PermanentDepositDrawingsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private PadDepositDrawingRepository padDepositDrawingRepository;

  @MockBean
  private DepositDrawingsService depositDrawingsService;

  @MockBean
  private PermanentDepositsDrawingValidator validator;

  @MockBean
  private PadFileManagementService padFileManagementService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;


  @BeforeEach
  void setUp() {
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetailByAppId(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any()))
        .thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
    when(padFileManagementService.getFileUploadComponentAttributesForLegacyPadFile(
        any(),
        any(),
        eq(FileDocumentType.DEPOSIT_DRAWINGS),
        eq(ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS)
    )).thenReturn(FileManagementControllerTestUtils.createUploadFileAttributes());
  }

  //Overview endpoint tests
  @Test
  void renderDepositDrawingsOverview_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(),  null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderDepositDrawingsOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderDepositDrawingsOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postDepositDrawingsOverview_appTypeSmokeTest() {
    when(depositDrawingsService.getDepositDrawingSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postDepositDrawingsOverview_appStatusSmokeTest() {
    when(depositDrawingsService.getDepositDrawingSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postDepositDrawingsOverview_permissionSmokeTest() {
    when(depositDrawingsService.getDepositDrawingSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  //ADD endpoint tests
  @Test
  void renderAddDepositDrawing_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(),  null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderAddDepositDrawing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddDepositDrawing_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postAddDepositDrawing_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddDepositDrawing_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postAddDepositDrawing_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postAddDepositDrawing_withInvalidForm() throws Exception {
    ControllerTestUtils.failValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("reference"));

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postAddDepositDrawing(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk());
  }

  @Test
  void postAddDepositDrawing_withValidForm() throws Exception {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postAddDepositDrawing(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().is3xxRedirection());

    verify(depositDrawingsService, times(1)).addDrawing(any(), any(), any());
    verify(depositDrawingsService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
  }

  //REMOVE end points
  @Test
  void renderRemovePermanentDeposits_success() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
        .renderRemoveDepositDrawing(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), null, 1, null)))
        .with(user(user))
        .with(csrf())
        .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  void renderRemoveDepositDrawing_permissionSmokeTest() {
    when(depositDrawingsService.getDepositDrawingView(any(), any())).thenReturn(buildDepositDrawingView());
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderRemoveDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(),  null, 1, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderRemoveDepositDrawing_appTypeSmokeTest() {
    when(depositDrawingsService.getDepositDrawingView(any(), any())).thenReturn(buildDepositDrawingView());
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderRemoveDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderRemoveDepositDrawing_appStatusSmokeTest() {
    when(depositDrawingsService.getDepositDrawingView(any(), any())).thenReturn(buildDepositDrawingView());
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderRemoveDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postRemoveDepositDrawing_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postRemoveDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postRemoveDepositDrawing_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postRemoveDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postRemoveDepositDrawing_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postRemoveDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  //EDIT endpoints
  @Test
  void renderEditDepositDrawing_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(),  null, 1, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderEditDepositDrawing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderEditDepositDrawing_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postEditDepositDrawing_appTypeSmokeTest() {
    when(depositDrawingsService.validateDrawingEdit(any(), any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditDepositDrawing_appStatusSmokeTest() {
    when(depositDrawingsService.validateDrawingEdit(any(), any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postEditDepositDrawing_permissionSmokeTest() {
    when(depositDrawingsService.validateDrawingEdit(any(), any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditDepositDrawing_withInvalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form");
    bindingResult.addError(new ObjectError("fake", "fake"));
    when(depositDrawingsService.validateDrawingEdit(any(), any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postEditDepositDrawing(PwaApplicationType.INITIAL, 1, null, 1, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk());
  }

  @Test
  void postEditDepositDrawing_withValidForm() throws Exception {
    when(depositDrawingsService.validateDrawingEdit(any(), any(), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postEditDepositDrawing(PwaApplicationType.INITIAL, 1, null, 1, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().is3xxRedirection());

    verify(depositDrawingsService, times(1)).editDepositDrawing(anyInt(), any(), any());
    verify(depositDrawingsService, times(1)).validateDrawingEdit(any(), any(), any(), anyInt());
  }

  private PermanentDepositDrawingView buildDepositDrawingView() {
    var view = new PermanentDepositDrawingView();
    view.setDepositDrawingId(1);
    view.setDepositReferences(Set.of("dep ref"));
    view.setReference("drawing ref");
    view.setFileId(String.valueOf(UUID.randomUUID()));
    view.setDocumentDescription("description");
    view.setFileName("file name");
    return view;
  }
}
