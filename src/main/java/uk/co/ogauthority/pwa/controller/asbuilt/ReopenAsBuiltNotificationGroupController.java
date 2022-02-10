package uk.co.ogauthority.pwa.controller.asbuilt;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTab;
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.controller.CaseManagementController;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.util.FlashUtils;

@Controller
@RequestMapping("/as-built-notification/{notificationGroupId}/reopen")
public class ReopenAsBuiltNotificationGroupController {

  private final AsBuiltNotificationAuthService asBuiltNotificationAuthService;
  private final AsBuiltViewerService asBuiltViewerService;
  private final AsBuiltInteractorService asBuiltInteractorService;

  public ReopenAsBuiltNotificationGroupController(
      AsBuiltNotificationAuthService asBuiltNotificationAuthService,
      AsBuiltViewerService asBuiltViewerService,
      AsBuiltInteractorService asBuiltInteractorService) {
    this.asBuiltNotificationAuthService = asBuiltNotificationAuthService;
    this.asBuiltViewerService = asBuiltViewerService;
    this.asBuiltInteractorService = asBuiltInteractorService;
  }

  @GetMapping
  public ModelAndView renderReopenAsBuiltNotificationForm(@PathVariable Integer notificationGroupId,
                                                          AuthenticatedUserAccount authenticatedUserAccount) {
    var notificationGroup = asBuiltViewerService.getNotificationGroup(notificationGroupId);
    var application = notificationGroup.getPwaConsent().getSourcePwaApplication();

    checkUserCanReopenAsBuiltGroup(authenticatedUserAccount, notificationGroup);

    var summary = asBuiltViewerService.getAsBuiltNotificationGroupSummaryView(notificationGroupId);
    return new ModelAndView("/asbuilt/form/reopenAsBuiltGroup")
        .addObject("notificationGroupSummaryView", summary)
        .addObject("cancelUrl", ReverseRouter.route(on(CaseManagementController.class)
            .renderCaseManagement(application.getId(), application.getApplicationType(), AppProcessingTab.TASKS, null, null)));
  }

  @PostMapping
  public ModelAndView reopenAsBuiltNotification(@PathVariable Integer notificationGroupId,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                RedirectAttributes redirectAttributes) {
    var notificationGroup = asBuiltViewerService.getNotificationGroup(notificationGroupId);
    var application = notificationGroup.getPwaConsent().getSourcePwaApplication();

    checkUserCanReopenAsBuiltGroup(authenticatedUserAccount, notificationGroup);

    asBuiltInteractorService.reopenAsBuiltNotificationGroup(notificationGroup, authenticatedUserAccount.getLinkedPerson());

    FlashUtils.success(redirectAttributes, String.format("Successfully reopened as-built notification group for application %s",
        application.getAppReference()));
    return ReverseRouter.redirect(on(CaseManagementController.class)
        .renderCaseManagement(application.getId(), application.getApplicationType(), AppProcessingTab.TASKS, null, null));
  }

  private void checkUserCanReopenAsBuiltGroup(AuthenticatedUserAccount user, AsBuiltNotificationGroup asBuiltNotificationGroup) {
    var person = user.getLinkedPerson();
    if (asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(person)
        && asBuiltViewerService.canGroupBeReopened(asBuiltNotificationGroup.getPwaConsent())) {
      return;
    }
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot reopen as-built notification groups as they either do not have " +
            "sufficient permission or the group cannot be opened at this time.", user.getWuaId()));
  }

}
