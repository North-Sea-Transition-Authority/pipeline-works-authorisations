package uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDeposit;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsForm;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsValidator;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinateUtils;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResultTestUtils;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@WebMvcTest(controllers = PermanentDepositController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PermanentDepositControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private PadPermanentDepositRepository padPermanentDepositRepository;

  @MockBean
  private PermanentDepositsValidator validator;

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
  }


  //ADD deposit tests
  @Test
  void renderAddPermanentDeposits_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderAddPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(),  null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderAddPermanentDeposits_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderAddPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderAddPermanentDeposits_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderAddPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postPermanentDeposits_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postPermanentDeposits_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postPermanentDeposits_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postPermanentDeposits_withInvalidForm() throws Exception {
    ControllerTestUtils.failValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromMonth"));

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk());
  }

  @Test
  void postPermanentDeposits_withValidForm() throws Exception {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().is3xxRedirection());

    verify(permanentDepositService, times(1)).saveEntityUsingForm(any(), any(), any());
    verify(permanentDepositService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
  }


  //EDIT deposit tests
  @Test
  void renderEditPermanentDeposits_permissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderEditPermanentDeposits_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderEditPermanentDeposits_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  void postEditPermanentDeposits_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditPermanentDeposits_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postEditPermanentDeposits_permissionSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postEditPermanentDeposits_withInvalidForm() throws Exception {
    ControllerTestUtils.failValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromMonth"));

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postEditPermanentDeposits(PwaApplicationType.INITIAL, 1, 1,null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk());
  }

  @Test
  void postEditPermanentDeposits_withValidForm() throws Exception {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postEditPermanentDeposits(PwaApplicationType.INITIAL, 1, 1, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().is3xxRedirection());

    verify(permanentDepositService, times(1)).saveEntityUsingForm(any(), any(), any());
    verify(permanentDepositService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
  }


  //OVERVIEW deposit tests
  @Test
  void renderPermanentDepositsOverview_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(),null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  void renderPermanentDepositsOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  void renderPermanentDepositsOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }


  @Test
  void postPermanentDepositsOverview_appTypeSmokeTest() {
    when(permanentDepositService.getDepositSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  void postPermanentDepositsOverview_appStatusSmokeTest() {
    when(permanentDepositService.getDepositSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  void postPermanentDepositsOverview_permissionSmokeTest() {
    when(permanentDepositService.getDepositSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.completeResult());
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  void postPermanentDepositsOverview_failValidation() {
    when(permanentDepositService.getDepositSummaryScreenValidationResult(any(PwaApplicationDetail.class)))
        .thenReturn(SummaryScreenValidationResultTestUtils.incompleteResult());
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText())
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }


  //Remove deposit tests
  @Test
  void renderRemovePermanentDeposits_success() throws Exception {
    when(padPermanentDepositRepository.findById(1)).thenReturn(Optional.of(buildDepositEntity()));

    mockMvc.perform(post(ReverseRouter.route(on(PermanentDepositController.class)
        .renderRemovePermanentDeposits(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), 1, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void postRemovePermanentDeposits_success() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PermanentDepositController.class)
        .postRemovePermanentDeposits(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), 1, null, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }



  public PadPermanentDeposit buildDepositEntity() {
    PadPermanentDeposit baseEntity = new PadPermanentDeposit();
    baseEntity.setId(1);
    baseEntity.setReference("my ref");
    baseEntity.setFromMonth(2);
    baseEntity.setFromYear(2020);
    baseEntity.setToMonth(3);
    baseEntity.setToYear(2020);

    baseEntity.setQuantity(Double.parseDouble("5.7"));
    baseEntity.setContingencyAmount("88");

    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    baseEntity.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(fromCoordinateForm));

    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    baseEntity.setToCoordinates(CoordinateUtils.coordinatePairFromForm(toCoordinateForm));
    return baseEntity;
  }


}
