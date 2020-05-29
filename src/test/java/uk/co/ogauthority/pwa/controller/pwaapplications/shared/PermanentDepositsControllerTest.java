package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;




@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PermanentDepositController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PermanentDepositsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private PermanentDepositInformationRepository padPermanentDepositRepository;

  @MockBean
  private PermanentDepositsValidator validator;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;


  @Before
  public void setUp() {
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaContactService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.OPTIONS_VARIATION,
            PwaApplicationType.DECOMMISSIONING)
        .setAllowedRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));
  }



  //ADD deposit tests
  @Test
  public void renderAddPermanentDeposits_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderAddPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(),  null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderAddPermanentDeposits_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderAddPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddPermanentDeposits_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderAddPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postPermanentDeposits_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postPermanentDeposits_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postPermanentDeposits_contactSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postPermanentDeposits_withInvalidForm() throws Exception {
    ControllerTestUtils.failValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromMonth"));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
  }

  @Test
  public void postPermanentDeposits_withValidForm() throws Exception {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postPermanentDeposits(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());

    verify(permanentDepositService, times(1)).saveEntityUsingForm(any(), any(), any());
    verify(permanentDepositService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
  }



  //EDIT deposit tests
  @Test
  public void renderEditPermanentDeposits_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderEditPermanentDeposits_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditPermanentDeposits_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postEditPermanentDeposits_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditPermanentDeposits_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postEditPermanentDeposits_contactSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postEditPermanentDeposits(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null, ValidationType.FULL)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditPermanentDeposits_withInvalidForm() throws Exception {
    ControllerTestUtils.failValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("fromMonth"));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postEditPermanentDeposits(PwaApplicationType.INITIAL, 1, 1,null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
  }

  @Test
  public void postEditPermanentDeposits_withValidForm() throws Exception {
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositController.class)
            .postEditPermanentDeposits(PwaApplicationType.INITIAL, 1, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());

    verify(permanentDepositService, times(1)).saveEntityUsingForm(any(), any(), any());
    verify(permanentDepositService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
  }




  //OVERVIEW deposit tests
  @Test
  public void renderPermanentDepositsOverview_contactSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(),null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderPermanentDepositsOverview_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderPermanentDepositsOverview_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .renderPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }



  @Test
  public void postPermanentDepositsOverview_appTypeSmokeTest() {
    when(permanentDepositService.validateDepositOverview(any(PwaApplicationDetail.class))).thenReturn(true);
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());
  }

  @Test
  public void postPermanentDepositsOverview_appStatusSmokeTest() {
    when(permanentDepositService.validateDepositOverview(any(PwaApplicationDetail.class))).thenReturn(true);
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postPermanentDepositsOverview_contactSmokeTest() {
    when(permanentDepositService.validateDepositOverview(any(PwaApplicationDetail.class))).thenReturn(true);
    ControllerTestUtils.passValidationWhenPost(permanentDepositService, new PermanentDepositsForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositController.class)
                .postPermanentDepositsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }



  //Remove deposit tests
  @Test
  public void renderRemovePermanentDeposits_success() throws Exception {
    when(padPermanentDepositRepository.findById(1)).thenReturn(Optional.of(buildDepositEntity()));

    mockMvc.perform(post(ReverseRouter.route(on(PermanentDepositController.class)
        .renderRemovePermanentDeposits(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), 1, null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void postRemovePermanentDeposits_success() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(PermanentDepositController.class)
        .postRemovePermanentDeposits(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(), 1, null, null, null)))
        .with(authenticatedUserAndSession(user))
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