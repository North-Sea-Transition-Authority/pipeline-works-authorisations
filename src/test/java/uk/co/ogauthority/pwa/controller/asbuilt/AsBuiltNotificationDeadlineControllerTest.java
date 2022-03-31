package uk.co.ogauthority.pwa.controller.asbuilt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.BEFORE_TODAY;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.form.asbuilt.ChangeAsBuiltNotificationGroupDeadlineForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltBreadCrumbService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.testutils.AsBuiltNotificationSummaryTestUtil;
import uk.co.ogauthority.pwa.validators.asbuilt.ChangeAsBuiltNotificationGroupDeadlineValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(AsBuiltNotificationDeadlineController.class)
@Import(PwaMvcTestConfiguration.class)
public class AsBuiltNotificationDeadlineControllerTest extends AbstractControllerTest {

  @MockBean
  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  @MockBean
  private AsBuiltViewerService asBuiltViewerService;

  @MockBean
  private ChangeAsBuiltNotificationGroupDeadlineValidator changeAsBuiltNotificationGroupDeadlineValidator;

  @SpyBean
  private AsBuiltBreadCrumbService asBuiltBreadCrumbService;

  @MockBean
  private AsBuiltInteractorService asBuiltInteractorService;

  private final AuthenticatedUserAccount user = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(1);

  private static final int NOTIFICATION_GROUP_ID  = 10;
  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_fromNgId(NOTIFICATION_GROUP_ID);

  @Before
  public void setup() {
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(true);
    when(asBuiltViewerService.getAsBuiltNotificationGroupSummaryView(NOTIFICATION_GROUP_ID)).thenReturn(
        AsBuiltNotificationSummaryTestUtil.getAsBuiltNotificationSummmary());
    when(asBuiltViewerService.getNotificationGroup(NOTIFICATION_GROUP_ID))
        .thenReturn(asBuiltNotificationGroup);
  }

  @Test
  public void renderAsBuiltGroupUpdateDeadlineForm_authorizedUser_successful() throws Exception {
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(true);
    mockMvc.perform(get(
        ReverseRouter.route(on(AsBuiltNotificationDeadlineController.class)
            .renderAsBuiltGroupUpdateDeadlineForm(NOTIFICATION_GROUP_ID, new ChangeAsBuiltNotificationGroupDeadlineForm(), user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("asbuilt/form/changeAsBuiltDeadline"));
  }

  @Test
  public void renderAsBuiltGroupUpdateDeadlineForm_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(false);
    mockMvc.perform(get(
        ReverseRouter.route(on(AsBuiltNotificationDeadlineController.class)
            .renderAsBuiltGroupUpdateDeadlineForm(NOTIFICATION_GROUP_ID, new ChangeAsBuiltNotificationGroupDeadlineForm(), user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void submitAsBuiltGroupUpdateDeadline_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(false);

    mockMvc.perform(post(
        ReverseRouter.route(on(AsBuiltNotificationDeadlineController.class)
            .submitAsBuiltGroupUpdateDeadline(NOTIFICATION_GROUP_ID, new ChangeAsBuiltNotificationGroupDeadlineForm(), null,
                null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void submitAsBuiltGroupUpdateDeadline_failsValidation() throws Exception {
    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("newDeadlineDateTimestampStr", BEFORE_TODAY.errorCode("newDeadlineDateTimestampStr"),
          "error message");
      return errors;
    }).when(changeAsBuiltNotificationGroupDeadlineValidator).validate(any(), any());

    mockMvc.perform(post(
        ReverseRouter.route(on(AsBuiltNotificationDeadlineController.class)
            .submitAsBuiltGroupUpdateDeadline(NOTIFICATION_GROUP_ID, new ChangeAsBuiltNotificationGroupDeadlineForm(), null,
                null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is2xxSuccessful())
        .andExpect(view().name("asbuilt/form/changeAsBuiltDeadline"));
  }

  @Test
  public void submitAsBuiltGroupUpdateDeadline_validationSuccess_returnToDashboard() throws Exception {
    mockMvc.perform(post(
        ReverseRouter.route(on(AsBuiltNotificationDeadlineController.class)
            .submitAsBuiltGroupUpdateDeadline(NOTIFICATION_GROUP_ID, new ChangeAsBuiltNotificationGroupDeadlineForm(), null,
                null, null)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(AsBuiltNotificationController.class).getAsBuiltNotificationDashboard(NOTIFICATION_GROUP_ID, user))));

    verify(asBuiltInteractorService)
        .setNewDeadlineDateForGroup(eq(asBuiltNotificationGroup), any(ChangeAsBuiltNotificationGroupDeadlineForm.class), eq(user));
  }

}