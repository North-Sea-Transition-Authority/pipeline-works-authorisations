package uk.co.ogauthority.pwa.controller.asbuilt;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.form.asbuilt.ChangeAsBuiltNotificationGroupDeadlineForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltBreadCrumbService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.validators.asbuilt.ChangeAsBuiltNotificationGroupDeadlineValidator;

@Controller
@RequestMapping("/as-built-notification/{notificationGroupId}/change-deadline")
public class AsBuiltNotificationDeadlineController {

  private final AsBuiltNotificationAuthService asBuiltNotificationAuthService;
  private final AsBuiltViewerService asBuiltViewerService;
  private final ChangeAsBuiltNotificationGroupDeadlineValidator changeAsBuiltNotificationGroupDeadlineValidator;
  private final AsBuiltBreadCrumbService asBuiltBreadCrumbService;
  private final AsBuiltInteractorService asBuiltInteractorService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public AsBuiltNotificationDeadlineController(
      AsBuiltNotificationAuthService asBuiltNotificationAuthService,
      AsBuiltViewerService asBuiltViewerService,
      ChangeAsBuiltNotificationGroupDeadlineValidator changeAsBuiltNotificationGroupDeadlineValidator,
      AsBuiltBreadCrumbService asBuiltBreadCrumbService,
      AsBuiltInteractorService asBuiltInteractorService,
      ControllerHelperService controllerHelperService) {
    this.asBuiltNotificationAuthService = asBuiltNotificationAuthService;
    this.asBuiltViewerService = asBuiltViewerService;
    this.changeAsBuiltNotificationGroupDeadlineValidator = changeAsBuiltNotificationGroupDeadlineValidator;
    this.asBuiltBreadCrumbService = asBuiltBreadCrumbService;
    this.asBuiltInteractorService = asBuiltInteractorService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderAsBuiltGroupUpdateDeadlineForm(@PathVariable Integer notificationGroupId,
                                                           @ModelAttribute("form") ChangeAsBuiltNotificationGroupDeadlineForm form,
                                                           AuthenticatedUserAccount authenticatedUserAccount) {
    checkUserCanChangeAsBuiltDeadline(authenticatedUserAccount, notificationGroupId);

    var summary = asBuiltViewerService
        .getAsBuiltNotificationGroupSummaryView(notificationGroupId);
    var modelAndView = new ModelAndView("asbuilt/form/changeAsBuiltDeadline")
        .addObject("asBuiltGroupReference", summary.getAppReference())
        .addObject("notificationGroupSummaryView", summary)
        .addObject("cancelUrl", ReverseRouter.route(on(AsBuiltNotificationController.class)
            .getAsBuiltNotificationDashboard(notificationGroupId, null)))
        .addObject("errorList", List.of());

    asBuiltBreadCrumbService.fromDashboard(notificationGroupId, summary.getAppReference(), modelAndView, "Change deadline");
    return modelAndView;
  }

  @PostMapping
  public ModelAndView submitAsBuiltGroupUpdateDeadline(@PathVariable Integer notificationGroupId,
                                                       @ModelAttribute("form") ChangeAsBuiltNotificationGroupDeadlineForm form,
                                                       BindingResult bindingResult,
                                                       RedirectAttributes redirectAttributes,
                                                       AuthenticatedUserAccount authenticatedUserAccount) {
    checkUserCanChangeAsBuiltDeadline(authenticatedUserAccount, notificationGroupId);

    var asBuiltNotificationGroup = getAsBuiltNotificationGroup(notificationGroupId);

    changeAsBuiltNotificationGroupDeadlineValidator.validate(form, bindingResult);
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        renderAsBuiltGroupUpdateDeadlineForm(notificationGroupId, form, authenticatedUserAccount),
        () -> {
          asBuiltInteractorService.setNewDeadlineDateForGroup(asBuiltNotificationGroup, form, authenticatedUserAccount);
          FlashUtils.success(redirectAttributes, "As-built notification group deadline updated");
          return ReverseRouter.redirect(on(AsBuiltNotificationController.class)
              .getAsBuiltNotificationDashboard(notificationGroupId, null));
        });
  }

  private void checkUserCanChangeAsBuiltDeadline(AuthenticatedUserAccount user, Integer notificationGroupId) {
    if (asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(user.getLinkedPerson())) {
      return;
    }
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot change deadlines for as-built notification groups as they do not have " +
            "sufficient permission.", user.getWuaId()));
  }

  private AsBuiltNotificationGroup getAsBuiltNotificationGroup(Integer asBuiltNotificationGroupId) {
    return asBuiltViewerService.getNotificationGroup(asBuiltNotificationGroupId);
  }

}