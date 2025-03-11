package uk.co.ogauthority.pwa.controller.asbuilt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltNotificationGroupSummaryView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.testutils.AsBuiltNotificationSummaryTestUtil;

@WebMvcTest(ReopenAsBuiltNotificationGroupController.class)
@Import(PwaMvcTestConfiguration.class)
class ReopenAsBuiltNotificationGroupControllerTest extends AbstractControllerTest {

  @MockBean
  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  @MockBean
  private AsBuiltViewerService asBuiltViewerService;

  @MockBean
  private AsBuiltInteractorService asBuiltInteractorService;

  private static final int NOTIFICATION_GROUP_ID  = 10;

  private final AuthenticatedUserAccount user = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(1);
  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil
      .createGroupWithConsent_withApplication_fromNgId(NOTIFICATION_GROUP_ID);
  private final AsBuiltNotificationGroupSummaryView summaryView = AsBuiltNotificationSummaryTestUtil.getAsBuiltNotificationSummmary();

  @BeforeEach
  void setup() {
    when(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(user)).thenReturn(true);
    when(asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(true);
    when(asBuiltViewerService.getNotificationGroup(NOTIFICATION_GROUP_ID)).thenReturn(asBuiltNotificationGroup);
    when(asBuiltViewerService.getAsBuiltNotificationGroupSummaryView(NOTIFICATION_GROUP_ID))
        .thenReturn(summaryView);
  }

  @Test
  void renderReopenAsBuiltNotificationForm_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(user)).thenReturn(false);
    when(asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(false);

    mockMvc.perform(get(
        ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
            .renderReopenAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, user)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderReopenAsBuiltNotificationForm_unauthorizedUser_groupCannotBeReopened_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(user)).thenReturn(true);
    when(asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(false);

    mockMvc.perform(get(
        ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
            .renderReopenAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, user)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderReopenAsBuiltNotificationForm_authorizedUser_success() throws Exception {
    mockMvc.perform(get(
        ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
            .renderReopenAsBuiltNotificationForm(NOTIFICATION_GROUP_ID, user)))
        .with(user(user)))
        .andExpect(status().is2xxSuccessful())
    .andExpect(view().name("/asbuilt/form/reopenAsBuiltGroup"));
  }

  @Test
  void reopenAsBuiltNotification_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(user)).thenReturn(false);
    when(asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(false);

    mockMvc.perform(post(
        ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
            .reopenAsBuiltNotification(NOTIFICATION_GROUP_ID, null, null)))
        .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void reopenAsBuiltNotification_authorizedUser_success() throws Exception {
    when(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(user)).thenReturn(true);
    when(asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent()))
        .thenReturn(true);

    mockMvc.perform(post(
        ReverseRouter.route(on(ReopenAsBuiltNotificationGroupController.class)
            .reopenAsBuiltNotification(NOTIFICATION_GROUP_ID, null, null)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(asBuiltInteractorService).reopenAsBuiltNotificationGroup(asBuiltNotificationGroup, user.getLinkedPerson());
  }

}
