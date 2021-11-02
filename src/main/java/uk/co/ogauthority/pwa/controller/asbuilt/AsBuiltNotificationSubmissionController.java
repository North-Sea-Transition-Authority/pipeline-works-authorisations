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
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.AsBuiltNotificationGroupNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupPipeline;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltSubmissionResult;
import uk.co.ogauthority.pwa.model.form.asbuilt.AsBuiltNotificationSubmissionForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltBreadCrumbService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationAuthService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltNotificationGroupService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltPipelineNotificationService;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.validators.asbuilt.AsBuiltNotificationSubmissionValidator;
import uk.co.ogauthority.pwa.validators.asbuilt.AsBuiltNotificationSubmissionValidatorHint;

@Controller
@RequestMapping("/as-built-notification/{notificationGroupId}/as-built-submission/{pipelineDetailId}")
public class AsBuiltNotificationSubmissionController {

  private final AsBuiltNotificationAuthService asBuiltNotificationAuthService;
  private final AsBuiltNotificationGroupService asBuiltNotificationGroupService;
  private final AsBuiltPipelineNotificationService asBuiltPipelineNotificationService;
  private final AsBuiltInteractorService asBuiltInteractorService;
  private final AsBuiltViewerService asBuiltViewerService;
  private final AsBuiltNotificationSubmissionValidator asBuiltNotificationSubmissionValidator;
  private final ControllerHelperService controllerHelperService;
  private final AsBuiltBreadCrumbService asBuiltBreadCrumbService;

  @Autowired
  public AsBuiltNotificationSubmissionController(
      AsBuiltNotificationAuthService asBuiltNotificationAuthService,
      AsBuiltNotificationGroupService asBuiltNotificationGroupService,
      AsBuiltPipelineNotificationService asBuiltPipelineNotificationService,
      AsBuiltInteractorService asBuiltInteractorService,
      AsBuiltViewerService asBuiltViewerService,
      AsBuiltNotificationSubmissionValidator asBuiltNotificationSubmissionValidator,
      ControllerHelperService controllerHelperService,
      AsBuiltBreadCrumbService asBuiltBreadCrumbService) {

    this.asBuiltNotificationAuthService = asBuiltNotificationAuthService;
    this.asBuiltNotificationGroupService = asBuiltNotificationGroupService;
    this.asBuiltPipelineNotificationService = asBuiltPipelineNotificationService;
    this.asBuiltInteractorService = asBuiltInteractorService;
    this.asBuiltViewerService = asBuiltViewerService;
    this.asBuiltNotificationSubmissionValidator = asBuiltNotificationSubmissionValidator;
    this.controllerHelperService = controllerHelperService;
    this.asBuiltBreadCrumbService = asBuiltBreadCrumbService;
  }

  @GetMapping
  public ModelAndView renderSubmitAsBuiltNotificationForm(@PathVariable Integer notificationGroupId,
                                                          @PathVariable Integer pipelineDetailId,
                                                          AuthenticatedUserAccount authenticatedUserAccount,
                                                          @ModelAttribute("form") AsBuiltNotificationSubmissionForm form) {
    checkUserCanAccessAsBuiltNotification(authenticatedUserAccount, notificationGroupId);
    var pipelineDetail = asBuiltPipelineNotificationService.getPipelineDetail(pipelineDetailId);
    var asBuiltNotificationGroup = asBuiltNotificationGroupService.getAsBuiltNotificationGroup(notificationGroupId).orElseThrow(
        () ->
            new AsBuiltNotificationGroupNotFoundException(String.format("Could not find as-built notification group with id %s",
                notificationGroupId)));
    return getSubmitAsBuiltNotificationModelAndView(authenticatedUserAccount.getLinkedPerson(), pipelineDetail,
        asBuiltNotificationGroup);
  }

  @PostMapping
  public ModelAndView postSubmitAsBuiltNotification(@PathVariable Integer notificationGroupId,
                                                    @PathVariable Integer pipelineDetailId,
                                                    AuthenticatedUserAccount authenticatedUserAccount,
                                                    @ModelAttribute("form") AsBuiltNotificationSubmissionForm form,
                                                    BindingResult bindingResult,
                                                    RedirectAttributes redirectAttributes) {
    checkUserCanAccessAsBuiltNotification(authenticatedUserAccount, notificationGroupId);
    var pipelineDetail = getPipelineDetail(pipelineDetailId);
    var isPersonOgaUser = isPersonOgaUser(authenticatedUserAccount.getLinkedPerson());
    var asBuiltNotificationGroupPipeline = getAsBuiltNotificationGroupPipeline(notificationGroupId,
        pipelineDetail.getPipelineDetailId());
    var asBuiltNotificationGroup = asBuiltNotificationGroupPipeline.getAsBuiltNotificationGroup();

    checkUserCanAccessAsBuiltNotification(authenticatedUserAccount, notificationGroupId);
    checkAsBuiltNotificationCanBeSubmitted(asBuiltNotificationGroup);

    asBuiltNotificationSubmissionValidator.validate(
        form,
        bindingResult,
        new AsBuiltNotificationSubmissionValidatorHint(isPersonOgaUser, asBuiltNotificationGroupPipeline.getPipelineChangeCategory()));
    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        renderSubmitAsBuiltNotificationForm(notificationGroupId, pipelineDetailId, authenticatedUserAccount, form),
        () -> {
          var submissionResult = asBuiltInteractorService.submitAsBuiltNotification(asBuiltNotificationGroupPipeline,
              form, authenticatedUserAccount);

          return asBuiltSubmissionSuccessRedirect(asBuiltNotificationGroup, submissionResult, redirectAttributes);
        });
  }

  private ModelAndView getSubmitAsBuiltNotificationModelAndView(Person person,
                                                               PipelineDetail pipelineDetail,
                                                               AsBuiltNotificationGroup asBuiltNotificationGroup) {
    var modelAndView = new ModelAndView("asbuilt/form/submitAsBuiltNotification");
    var pipelineChangeCategory = getPipelineChangeCategory(asBuiltNotificationGroup.getId(),
        pipelineDetail.getPipelineDetailId());
    var isPersonOgaUser = isPersonOgaUser(person);
    var cancelUrl = ReverseRouter.route(on(AsBuiltNotificationController.class)
        .getAsBuiltNotificationDashboard(asBuiltNotificationGroup.getId(), null));
    var summary = asBuiltViewerService
        .getAsBuiltNotificationGroupSummaryView(asBuiltNotificationGroup.getId());
    modelAndView
        .addObject("notificationGroupSummaryView", summary)
        .addObject("pipelineNumber", pipelineDetail.getPipelineNumber())
        .addObject("consentedPipelineStatus", pipelineDetail.getPipelineStatus().getDisplayText())
        .addObject("pipelineChangeCategory", pipelineChangeCategory)
        .addObject("asBuiltStatusOptions", AsBuiltNotificationStatus.asList(pipelineDetail.getPipelineStatus()))
        .addObject("isOgaUser", isPersonOgaUser)
        .addObject("cancelUrl", cancelUrl)
        .addObject("errorList", List.of());
    asBuiltBreadCrumbService.fromDashboard(asBuiltNotificationGroup.getId(), asBuiltNotificationGroup.getReference(), modelAndView,
        pipelineDetail.getPipelineNumber() + " notification");
    return modelAndView;
  }

  private ModelAndView asBuiltSubmissionSuccessRedirect(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                        AsBuiltSubmissionResult asBuiltSubmissionResult,
                                                        RedirectAttributes redirectAttributes) {
    if (asBuiltSubmissionResult == AsBuiltSubmissionResult.AS_BUILT_GROUP_COMPLETED) {
      FlashUtils.success(redirectAttributes, String.format("All as-built notifications for application %s have been successfully submitted",
          asBuiltNotificationGroup.getReference()));
      return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.AS_BUILT_NOTIFICATIONS, null));
    }

    FlashUtils.success(redirectAttributes, "As-built notification submitted.");
    return ReverseRouter.redirect(on(AsBuiltNotificationController.class)
        .getAsBuiltNotificationDashboard(asBuiltNotificationGroup.getId(), null));
  }

  private void checkAsBuiltNotificationCanBeSubmitted(AsBuiltNotificationGroup asBuiltNotificationGroup) {
    if (!asBuiltViewerService.isGroupStatusComplete(asBuiltNotificationGroup)) {
      return;
    }
    throw new AccessDeniedException(
        String.format("Cannot perform any more submissions for as-built notification group with ID %s as the group status is complete",
            asBuiltNotificationGroup.getId()));
  }

  private void checkUserCanAccessAsBuiltNotification(AuthenticatedUserAccount user, Integer notificationGroupId) {
    if (asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(user.getLinkedPerson(), notificationGroupId)) {
      return;
    }
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot access the as-built notification as they either do not have " +
                "sufficient permission and/or they are not a member of the holder organisation group of the application.",
            user.getWuaId()));
  }

  private PipelineDetail getPipelineDetail(Integer pipelineDetailId) {
    return asBuiltPipelineNotificationService.getPipelineDetail(pipelineDetailId);
  }

  private boolean isPersonOgaUser(Person person) {
    return asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(person);
  }

  private PipelineChangeCategory getPipelineChangeCategory(Integer asBuiltNotificationGroupId, PipelineDetailId pipelineDetailId) {
    return asBuiltPipelineNotificationService.getAsBuiltNotificationGroupPipeline(asBuiltNotificationGroupId, pipelineDetailId)
        .getPipelineChangeCategory();
  }

  private AsBuiltNotificationGroupPipeline getAsBuiltNotificationGroupPipeline(Integer asBuiltNotificationGroupId,
                                                                               PipelineDetailId pipelineDetailId) {
    return asBuiltPipelineNotificationService.getAsBuiltNotificationGroupPipeline(asBuiltNotificationGroupId, pipelineDetailId);
  }

}