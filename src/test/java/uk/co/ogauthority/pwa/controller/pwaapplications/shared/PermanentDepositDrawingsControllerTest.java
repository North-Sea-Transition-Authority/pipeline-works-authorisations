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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.ControllerTestUtils;
import uk.co.ogauthority.pwa.util.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.PermanentDepositsDrawingValidator;

import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;


@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PermanentDepositDrawingsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PermanentDepositDrawingsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private DepositDrawingsService depositDrawingsService;

  @MockBean
  private PermanentDepositsDrawingValidator validator;


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
        .setAllowedContactRoles(PwaContactRole.PREPARER)
        .setAllowedStatuses(PwaApplicationStatus.DRAFT);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplicationDetail.getPwaApplication().setId(APP_ID);
    when(pwaApplicationDetailService.getTipDetail(pwaApplicationDetail.getMasterPwaApplicationId())).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any()))
        .thenReturn(EnumSet.allOf(PwaContactRole.class));
  }




  //Overview endpoint tests
  @Test
  public void renderDepositDrawingsOverview_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(),  null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderDepositDrawingsOverview_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderDepositDrawingsOverview_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postDepositDrawingsOverview_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postDepositDrawingsOverview_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postDepositDrawingsOverview_contactSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postDepositDrawingsOverview(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }




  //ADD endpoint tests
  @Test
  public void renderAddDepositDrawing_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(),  null, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderAddDepositDrawing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddDepositDrawing_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postAddDepositDrawing_appTypeSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddDepositDrawing_appStatusSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postAddDepositDrawing_contactSmokeTest() {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postAddDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, ValidationType.FULL)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postAddDepositDrawing_withInvalidForm() throws Exception {
    ControllerTestUtils.failValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("reference"));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postAddDepositDrawing(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
  }

  @Test
  public void postAddDepositDrawing_withValidForm() throws Exception {
    ControllerTestUtils.passValidationWhenPost(depositDrawingsService, new PermanentDepositDrawingForm(), ValidationType.FULL );
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postAddDepositDrawing(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());

    verify(depositDrawingsService, times(1)).addDrawing(any(), any(), any());
    verify(depositDrawingsService, times(1)).validate(any(), any(), eq(ValidationType.FULL), any());
  }




  //EDIT endpoints
  @Test
  public void renderEditDepositDrawing_contactSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(),  null, 1, null)));

    endpointTester.performAppContactRoleCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderEditDepositDrawing_appTypeSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderEditDepositDrawing_appStatusSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .renderEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void postEditDepositDrawing_appTypeSmokeTest() {
    when(depositDrawingsService.validate(any(), any(), eq(ValidationType.FULL), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null, ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditDepositDrawing_appStatusSmokeTest() {
    when(depositDrawingsService.validate(any(), any(), eq(ValidationType.FULL), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null, ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void postEditDepositDrawing_contactSmokeTest() {
    when(depositDrawingsService.validate(any(), any(), eq(ValidationType.FULL), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam("Complete", "Complete")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PermanentDepositDrawingsController.class)
                .postEditDepositDrawing(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null, ValidationType.FULL)));

    endpointTester.performAppContactRoleCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void postEditDepositDrawing_withInvalidForm() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form");
    bindingResult.addError(new ObjectError("fake", "fake"));
    when(depositDrawingsService.validate(any(), any(), eq(ValidationType.FULL), any(), any())).thenReturn(bindingResult);
    ControllerTestUtils.mockSmartValidatorErrors(validator, List.of("reference"));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postEditDepositDrawing(PwaApplicationType.INITIAL, 1, null, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().isOk());
  }

  @Test
  public void postEditDepositDrawing_withValidForm() throws Exception {
    when(depositDrawingsService.validate(any(), any(), eq(ValidationType.FULL), any(), any())).thenReturn(new BeanPropertyBindingResult(new PermanentDepositDrawingForm(), "form"));
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>() {{
      add("Complete", "Complete");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .postEditDepositDrawing(PwaApplicationType.INITIAL, 1, null, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(params))
        .andExpect(status().is3xxRedirection());

    verify(depositDrawingsService, times(1)).editDepositDrawing(anyInt(), any(), any(), any());
    verify(depositDrawingsService, times(1)).validate(any(), any(), any(), any(), anyInt());
  }





  


}