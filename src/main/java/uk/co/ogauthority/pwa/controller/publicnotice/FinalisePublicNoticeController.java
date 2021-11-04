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
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.FinalisePublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/finalise-public-notice")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.FINALISE_PUBLIC_NOTICE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class FinalisePublicNoticeController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final FinalisePublicNoticeService finalisePublicNoticeService;
  private final ControllerHelperService controllerHelperService;


  @Autowired
  public FinalisePublicNoticeController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      ControllerHelperService controllerHelperService,
      FinalisePublicNoticeService withdrawPublicNoticeService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.finalisePublicNoticeService = withdrawPublicNoticeService;
  }


  private ModelAndView publicNoticeInValidState(PwaAppProcessingContext processingContext,
                                                Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (finalisePublicNoticeService.publicNoticeCanBeFinalised(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(
              String.format("Access denied as there is not a public notice in the case officer review stage for application with id: %s",
                  processingContext.getMasterPwaApplicationId()));
        });
  }


  private ModelAndView publicNoticeInValidStateForUpdate(PwaAppProcessingContext processingContext,
                                                         Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (finalisePublicNoticeService.publicNoticeDatesCanBeUpdated(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(String.format(
              "Access denied as there is not a finalised public notice that is waiting to be published for application with id: %s",
                  processingContext.getMasterPwaApplicationId()));
        });
  }



  @GetMapping
  public ModelAndView renderFinalisePublicNotice(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                @ModelAttribute("form") FinalisePublicNoticeForm form) {

    return publicNoticeInValidState(processingContext, () ->
        getFinalisePublicNoticeModelAndView(processingContext, PublicNoticeAction.FINALISE));
  }


  @PostMapping
  public ModelAndView postFinalisePublicNotice(@PathVariable("applicationId") Integer applicationId,
                                               @PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               PwaAppProcessingContext processingContext,
                                               AuthenticatedUserAccount authenticatedUserAccount,
                                               @ModelAttribute("form") FinalisePublicNoticeForm form,
                                               BindingResult bindingResult) {

    return publicNoticeInValidState(processingContext, () -> {
      var validatedBindingResult = finalisePublicNoticeService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getFinalisePublicNoticeModelAndView(processingContext, PublicNoticeAction.FINALISE), () -> {

            finalisePublicNoticeService.finalisePublicNotice(processingContext.getPwaApplication(), form, authenticatedUserAccount);
            return  ReverseRouter.redirect(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
                applicationId, pwaApplicationType, processingContext, authenticatedUserAccount));
          });
    });

  }


  @GetMapping("update")
  public ModelAndView renderUpdatePublicNoticePublicationDates(@PathVariable("applicationId") Integer applicationId,
                                                               @PathVariable("applicationType")
                                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                               PwaAppProcessingContext processingContext,
                                                               AuthenticatedUserAccount authenticatedUserAccount,
                                                               @ModelAttribute("form") FinalisePublicNoticeForm form) {

    return publicNoticeInValidStateForUpdate(processingContext, () -> {
      finalisePublicNoticeService.mapUnpublishedPublicNoticeDateToForm(processingContext.getPwaApplication(), form);
      return getFinalisePublicNoticeModelAndView(processingContext, PublicNoticeAction.UPDATE_DATES);
    });
  }


  @PostMapping("update")
  public ModelAndView postUpdatePublicNoticePublicationDates(@PathVariable("applicationId") Integer applicationId,
                                                             @PathVariable("applicationType")
                                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                             PwaAppProcessingContext processingContext,
                                                             AuthenticatedUserAccount authenticatedUserAccount,
                                                             @ModelAttribute("form") FinalisePublicNoticeForm form,
                                                             BindingResult bindingResult) {

    return publicNoticeInValidStateForUpdate(processingContext, () -> {
      var validatedBindingResult = finalisePublicNoticeService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getFinalisePublicNoticeModelAndView(processingContext, PublicNoticeAction.UPDATE_DATES), () -> {

            finalisePublicNoticeService.updatePublicNoticeDate(processingContext.getPwaApplication(), form, authenticatedUserAccount);
            return  ReverseRouter.redirect(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
                applicationId, pwaApplicationType, processingContext, authenticatedUserAccount));
          });
    });

  }


  private ModelAndView getFinalisePublicNoticeModelAndView(PwaAppProcessingContext processingContext,
                                                           PublicNoticeAction publicNoticeAction) {

    var pwaApplication = processingContext.getPwaApplication();
    var cancelUrl = ReverseRouter.route(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
        pwaApplication.getId(), pwaApplication.getApplicationType(), processingContext, null));

    var modelAndView = new ModelAndView("publicNotice/finalisePublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("cancelUrl", cancelUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("publicNoticeAction", publicNoticeAction);

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Finalise public notice");
    return modelAndView;
  }




}