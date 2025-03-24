package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDraftService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice-draft")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.DRAFT_PUBLIC_NOTICE})
public class PublicNoticeDraftController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDraftService publicNoticeDraftService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PublicNoticeDraftController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService,
      PublicNoticeDraftService publicNoticeDraftService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.controllerHelperService = controllerHelperService;
    this.publicNoticeDraftService = publicNoticeDraftService;
  }

  private ModelAndView publicNoticeInValidState(PwaAppProcessingContext processingContext,
                                                Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (publicNoticeService.canCreatePublicNoticeDraft(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(
              String.format("Access denied as the latest public notice does not meet the requirements to allow creating a new draft " +
                  "for application with id: %s", processingContext.getMasterPwaApplicationId()));
        });
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
  public ModelAndView renderDraftPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") PublicNoticeDraftForm form) {

    return publicNoticeInValidState(
        processingContext, () -> {
          var pwaApplication = processingContext.getPwaApplication();
          publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, form);
          return getDraftPublicNoticeModelAndView(processingContext, form);
        });
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
  public ModelAndView postDraftPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") PublicNoticeDraftForm form,
                                              BindingResult bindingResult) {

    return publicNoticeInValidState(processingContext, () -> {
      var validatedBindingResult = publicNoticeService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getDraftPublicNoticeModelAndView(processingContext, form), () -> {
            publicNoticeDraftService.submitPublicNoticeDraft(
                form, processingContext.getPwaApplication(), authenticatedUserAccount);
            return  ReverseRouter.redirect(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
                applicationId, pwaApplicationType, processingContext, authenticatedUserAccount));
          });

    });

  }

  private ModelAndView getDraftPublicNoticeModelAndView(PwaAppProcessingContext processingContext, PublicNoticeDraftForm form) {

    var pwaApplication = processingContext.getPwaApplication();

    var publicNoticeOverviewUrl = ReverseRouter.route(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
        pwaApplication.getId(), pwaApplication.getApplicationType(), null, null));

    var fileUploadAttributes = publicNoticeService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplication
    );

    var modelAndView = new ModelAndView("publicNotice/draftPublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("publicNoticeRequestReasons", PublicNoticeRequestReason.asList())
        .addObject("cancelUrl", publicNoticeOverviewUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("fileUploadAttributes", fileUploadAttributes);

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Draft a public notice");
    return modelAndView;
  }
}