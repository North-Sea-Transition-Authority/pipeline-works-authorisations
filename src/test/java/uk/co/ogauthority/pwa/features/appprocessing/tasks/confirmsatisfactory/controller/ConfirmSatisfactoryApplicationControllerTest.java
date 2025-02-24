package uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.MAX_LENGTH_EXCEEDED;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaAppProcessingContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.ProcessingPermissionsDto;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.ConfirmSatisfactoryApplicationFormValidator;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@WebMvcTest(controllers = ConfirmSatisfactoryApplicationController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaAppProcessingContextService.class}))
class ConfirmSatisfactoryApplicationControllerTest extends PwaAppProcessingContextAbstractControllerTest {

  private static final int APP_ID = 5;
  private static final int APP_DETAIL_ID = 50;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;

  private PwaApplicationEndpointTestBuilder endpointTester;

  @MockBean
  private ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;

  @MockBean
  private PwaAppProcessingPermissionService pwaAppProcessingPermissionService;

  @MockBean
  private ConfirmSatisfactoryApplicationFormValidator confirmSatisfactoryFormValidator;

  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private Person person;

  @BeforeEach
  void setUp() {

    person = PersonTestUtil.createPersonFrom(new PersonId(1));
    user = new AuthenticatedUserAccount(
        new WebUserAccount(1, person),
        EnumSet.allOf(PwaUserPrivilege.class));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL,
        APP_ID, APP_DETAIL_ID);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaAppProcessingPermissionService.getProcessingPermissionsDto(pwaApplicationDetail, user))
        .thenReturn(new ProcessingPermissionsDto(null, EnumSet.of(PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION)));

    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(confirmSatisfactoryApplicationService.taskAccessible(any())).thenReturn(true);

    endpointTester = new PwaApplicationEndpointTestBuilder(
        mockMvc,
        pwaApplicationDetailService,
        pwaAppProcessingPermissionService)
        .setAllowedStatuses(PwaApplicationStatus.CASE_OFFICER_REVIEW)
        .setAllowedProcessingPermissions(PwaAppProcessingPermission.CONFIRM_SATISFACTORY_APPLICATION);

  }

  @Test
  void renderConfirmSatisfactory_appStatusSmokeTest() {
    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
                .renderConfirmSatisfactory(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void renderConfirmSatisfactory_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
                .renderConfirmSatisfactory(applicationDetail.getMasterPwaApplicationId(), type, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());
  }


  @Test
  void confirmSatisfactory_appStatusSmokeTest() {
    mockConfirmSatisfactoryValidationFail();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
                .confirmSatisfactory(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    endpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());
  }

  @Test
  void confirmSatisfactory_processingPermissionSmokeTest() {
    mockConfirmSatisfactoryValidationFail();
    endpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
                .confirmSatisfactory(applicationDetail.getMasterPwaApplicationId(), type, null, null, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void confirmSatisfactory_whenTaskNotAccessible() throws Exception {
    when(confirmSatisfactoryApplicationService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(post(ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
        .confirmSatisfactory(APP_ID, APP_TYPE, null, null, null, null, null)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderConfirmSatisfactory_whenTaskNotAccessible() throws Exception {
    when(confirmSatisfactoryApplicationService.taskAccessible(any())).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
        .renderConfirmSatisfactory(APP_ID, APP_TYPE, null, null, null)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void confirmSatisfactory_whenTaskAccessible_andfailsValidation() throws Exception {
    mockConfirmSatisfactoryValidationFail();

    mockMvc.perform(post(ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
        .confirmSatisfactory(APP_ID, APP_TYPE, null, null, null, null, null)))
        .with(user(user))
        .param("reason", "val")
        .with(csrf()))
        .andExpect(status().isOk());

    verify(confirmSatisfactoryApplicationService, times(0)).confirmSatisfactory(any(), any(), any());
  }

  @Test
  void confirmSatisfactory_whenTaskAccessible_andPassesValidation() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
        .confirmSatisfactory(APP_ID, APP_TYPE, null, null, null, null, null)))
        .with(user(user))
        .param("reason", "val")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(confirmSatisfactoryApplicationService, times(1)).confirmSatisfactory(
        pwaApplicationDetail,
        "val",
        user.getLinkedPerson()
    );

  }

  @Test
  void renderConfirmSatisfactory_whenTaskAccessible() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ConfirmSatisfactoryApplicationController.class)
        .renderConfirmSatisfactory(APP_ID, APP_TYPE, null, null, null)))
        .with(user(user)))
        .andExpect(status().isOk());
  }

  private void mockConfirmSatisfactoryValidationFail() {
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("reason", MAX_LENGTH_EXCEEDED.errorCode("reason"), "error message");
      return errors;
    }).when(confirmSatisfactoryFormValidator).validate(any(), any());
  }
}