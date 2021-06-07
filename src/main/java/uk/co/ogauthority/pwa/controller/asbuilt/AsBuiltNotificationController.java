package uk.co.ogauthority.pwa.controller.asbuilt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;

@Controller
@RequestMapping("/as-built-notification/{notificationGroupId}")
public class AsBuiltNotificationController {

  private final AsBuiltNotificationAuthService asBuiltNotificationAuthService;
  private final AsBuiltViewerService asBuiltViewerService;

  @Autowired
  public AsBuiltNotificationController(
      AsBuiltNotificationAuthService asBuiltNotificationAuthService,
      AsBuiltViewerService asBuiltViewerService) {
    this.asBuiltNotificationAuthService = asBuiltNotificationAuthService;
    this.asBuiltViewerService = asBuiltViewerService;
  }

  @GetMapping
  public ModelAndView getAsBuiltNotificationDashboard(@PathVariable Integer notificationGroupId,
                                                      AuthenticatedUserAccount authenticatedUserAccount) {
    checkUserCanAccessAsBuiltNotificationDashboard(authenticatedUserAccount, notificationGroupId);
    var summary = asBuiltViewerService
        .getAsBuiltNotificationGroupSummaryView(notificationGroupId);
    var pipelineAsBuiltSubmissionViews = asBuiltViewerService
        .getAsBuiltPipelineNotificationSubmissionViews(notificationGroupId);
    return new ModelAndView("asbuilt/asBuiltDashboard")
        .addObject("notificationGroupSummaryView", summary)
        .addObject("pipelineAsBuiltSubmissionViews", pipelineAsBuiltSubmissionViews);
  }

  private void checkUserCanAccessAsBuiltNotificationDashboard(AuthenticatedUserAccount user, Integer notificationGroupId) {
    if (asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), notificationGroupId)) {
      return;
    }
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot access the as-built notification dashboard as they either do not have " +
                "sufficient permission and/or they are not a member of the holder organisation group of the application.",
            user.getWuaId()));
  }

}
