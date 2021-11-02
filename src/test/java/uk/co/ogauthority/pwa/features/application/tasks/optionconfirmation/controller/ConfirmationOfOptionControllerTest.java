package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOption;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConfirmationOfOptionController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class ConfirmationOfOptionControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Integer APP_ID = 1;

  @MockBean
  private PadConfirmationOfOptionService padConfirmationOfOptionService;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  private PwaApplicationEndpointTestBuilder endpointTester;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION,
        APP_ID);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        EnumSet.allOf(PwaUserPrivilege.class));

    endpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.INDUSTRY_EDITABLE);

    when(pwaApplicationPermissionService.getPermissions(any(), any())).thenReturn(Set.of(PwaApplicationPermission.EDIT));
    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
  }

  @Test
  public void renderConfirmOption_permissionSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmationOfOptionController.class)
                .renderConfirmOption(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void renderConfirmOption_appTypeSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmationOfOptionController.class)
                .renderConfirmOption(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderConfirmOption_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmationOfOptionController.class)
                .renderConfirmOption(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderConfirmOption_whenPadConfirmationOfOptionFound() throws Exception {
    var confirmation = new PadConfirmationOfOption();
    when(padConfirmationOfOptionService.findPadConfirmationOfOption(pwaApplicationDetail))
        .thenReturn(Optional.of(confirmation));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConfirmationOfOptionController.class)
            .renderConfirmOption(PwaApplicationType.OPTIONS_VARIATION, APP_ID, null, null)))
            .with(authenticatedUserAndSession(user))
    ).andExpect(status().isOk());

    verify(padConfirmationOfOptionService, times(1)).mapEntityToForm(any(), eq(confirmation));

  }


  @Test
  public void confirmOption_whenFailsValidation_andFullValidation() throws Exception {
    mockFailValidation();

    mockMvc.perform(
        post(ReverseRouter.route(on(ConfirmationOfOptionController.class)
            .confirmOption(PwaApplicationType.OPTIONS_VARIATION, APP_ID, null, null, null, null)))
            .param(ValidationType.FULL.getButtonText(), "")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().isOk());

    verify(padConfirmationOfOptionService, times(1)).validate(any(), any(), eq(ValidationType.FULL),
        eq(pwaApplicationDetail));

    verify(padConfirmationOfOptionService, times(0)).mapFormToEntity(any(), any());
    verify(padConfirmationOfOptionService, times(0)).savePadConfirmation(any());

  }

  @Test
  public void confirmOption_whenFailsValidation_andPartialValidation() throws Exception {
    mockFailValidation();

    mockMvc.perform(
        post(ReverseRouter.route(on(ConfirmationOfOptionController.class)
            .confirmOption(PwaApplicationType.OPTIONS_VARIATION, APP_ID, null, null, null, null)))
            .param(ValidationType.PARTIAL.getButtonText(), "")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().isOk());

    verify(padConfirmationOfOptionService, times(1)).validate(any(), any(), eq(ValidationType.PARTIAL),
        eq(pwaApplicationDetail));

    verify(padConfirmationOfOptionService, times(0)).mapFormToEntity(any(), any());
    verify(padConfirmationOfOptionService, times(0)).savePadConfirmation(any());

  }

  @Test
  public void confirmOption_whenPassesValidation() throws Exception {

    //return original binding result without errors
    when(padConfirmationOfOptionService.validate(any(), any(), any(), any()))
        .thenAnswer(invocation -> invocation.getArgument(1));
    var confirmation = new PadConfirmationOfOption();

    when(padConfirmationOfOptionService.getOrCreatePadConfirmationOfOption(pwaApplicationDetail))
        .thenReturn(confirmation);


    mockMvc.perform(
        post(ReverseRouter.route(on(ConfirmationOfOptionController.class)
            .confirmOption(PwaApplicationType.OPTIONS_VARIATION, APP_ID, null, null, null, null)))
            .param(ValidationType.FULL.getButtonText(), "")
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());


    verify(padConfirmationOfOptionService, times(1)).mapFormToEntity(any(), eq(confirmation));
    verify(padConfirmationOfOptionService, times(1)).savePadConfirmation(confirmation);

  }

  @Test
  public void confirmOption_permissionSmokeTest() {
    mockFailValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmationOfOptionController.class)
                .confirmOption(type, applicationDetail.getMasterPwaApplicationId(), null, null, null,
                    ValidationType.FULL)));

    endpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());
  }

  @Test
  public void confirmOption_appTypeSmokeTest() {
    mockFailValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmationOfOptionController.class)
                .confirmOption(type, applicationDetail.getMasterPwaApplicationId(), null, null, null,
                    ValidationType.FULL)));

    endpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void confirmOption_appStatusSmokeTest() {
    mockFailValidation();

    endpointTester.setRequestMethod(HttpMethod.POST)
        .addRequestParam(ValidationType.FULL.getButtonText(), "")
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmationOfOptionController.class)
                .confirmOption(type, applicationDetail.getMasterPwaApplicationId(), null, null, null,
                    ValidationType.FULL)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  private void mockFailValidation() {
    var bindingResult = mock(BindingResult.class);
    when(bindingResult.hasErrors()).thenReturn(true);

    when(padConfirmationOfOptionService.validate(any(), any(), any(), any()))
        .thenReturn(bindingResult);

  }
}