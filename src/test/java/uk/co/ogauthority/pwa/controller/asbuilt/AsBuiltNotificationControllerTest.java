package uk.co.ogauthority.pwa.controller.asbuilt;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltBreadCrumbService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.testutils.AsBuiltNotificationSummaryTestUtil;


@RunWith(SpringRunner.class)
@WebMvcTest(AsBuiltNotificationController.class)
public class AsBuiltNotificationControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  @MockBean
  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  @MockBean
  private AsBuiltViewerService asBuiltViewerService;

  @SpyBean
  private AsBuiltBreadCrumbService asBuiltBreadCrumbService;

  private final AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1),
      EnumSet.allOf(PwaUserPrivilege.class));

  private static final int NOTIFICATION_GROUP_ID  = 1;


  @Before
  public void setup() {
    when(asBuiltViewerService.getAsBuiltNotificationGroupSummaryView(NOTIFICATION_GROUP_ID)).thenReturn(
        AsBuiltNotificationSummaryTestUtil.getAsBuiltNotificationSummmary());
    when(asBuiltViewerService.getAsBuiltPipelineNotificationSubmissionViews(NOTIFICATION_GROUP_ID)).thenReturn(List.of());
  }

  @Test
  public void getAsBuiltNotificationDashboard_unauthorizedUser_forbidden() throws Exception {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), NOTIFICATION_GROUP_ID)).thenReturn(false);
    mockMvc.perform(get(ReverseRouter.route(on(AsBuiltNotificationController.class).getAsBuiltNotificationDashboard(NOTIFICATION_GROUP_ID, user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void getAsBuiltNotificationDashboard_authorizedUser_permitted() throws Exception {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson() ,NOTIFICATION_GROUP_ID)).thenReturn(true);
    mockMvc.perform(get(ReverseRouter.route(on(AsBuiltNotificationController.class).getAsBuiltNotificationDashboard(NOTIFICATION_GROUP_ID, user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());
  }

  @Test
  public void getAsBuiltNotificationDashboard_ogaUser_permitted_hasOgaFlag() throws Exception {
    when(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson() ,NOTIFICATION_GROUP_ID)).thenReturn(true);
    when(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())).thenReturn(true);
    mockMvc.perform(get(ReverseRouter.route(on(AsBuiltNotificationController.class).getAsBuiltNotificationDashboard(NOTIFICATION_GROUP_ID, user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("isOgaUser", true));
  }

}
