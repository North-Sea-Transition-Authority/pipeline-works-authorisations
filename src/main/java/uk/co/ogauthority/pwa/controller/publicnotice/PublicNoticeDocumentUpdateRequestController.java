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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDocumentUpdateRequestForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/request-public-notice-document-update")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.REQUEST_PUBLIC_NOTICE_UPDATE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeDocumentUpdateRequestController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDocumentUpdateRequestService publicNoticeDocumentUpdateRequestService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PublicNoticeDocumentUpdateRequestController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService,
      PublicNoticeDocumentUpdateRequestService publicNoticeDocumentUpdateRequestService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.controllerHelperService = controllerHelperService;
    this.publicNoticeDocumentUpdateRequestService = publicNoticeDocumentUpdateRequestService;
  }


  private ModelAndView publicNoticeInValidState(PwaAppProcessingContext processingContext,
                                                Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (publicNoticeDocumentUpdateRequestService.publicNoticeDocumentUpdateCanBeRequested(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(
              String.format("Access denied as there is not a public notice that requires a request for the document to be updated " +
                      "for application with id: %s", processingContext.getMasterPwaApplicationId()));
        });
  }


  @GetMapping
  public ModelAndView renderRequestPublicNoticeDocumentUpdate(@PathVariable("applicationId") Integer applicationId,
                                                              @PathVariable("applicationType")
                                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                              PwaAppProcessingContext processingContext,
                                                              AuthenticatedUserAccount authenticatedUserAccount,
                                                              @ModelAttribute("form") PublicNoticeDocumentUpdateRequestForm form) {

    return publicNoticeInValidState(processingContext, () ->
        getRequestPublicNoticeDocumentUpdateModelAndView(processingContext));
  }


  @PostMapping
  public ModelAndView postRequestPublicNoticeDocumentUpdate(@PathVariable("applicationId") Integer applicationId,
                                                            @PathVariable("applicationType")
                                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                            PwaAppProcessingContext processingContext,
                                                            AuthenticatedUserAccount authenticatedUserAccount,
                                                            @ModelAttribute("form") PublicNoticeDocumentUpdateRequestForm form,
                                                            BindingResult bindingResult) {

    return publicNoticeInValidState(processingContext, () -> {
      var validatedBindingResult = publicNoticeDocumentUpdateRequestService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getRequestPublicNoticeDocumentUpdateModelAndView(processingContext), () -> {

            publicNoticeDocumentUpdateRequestService.updatePublicNoticeDocumentAndTransitionWorkflow(
                processingContext.getPwaApplication(), form);
            return  ReverseRouter.redirect(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
                applicationId, pwaApplicationType, processingContext, authenticatedUserAccount));
          });
    });

  }



  private ModelAndView getRequestPublicNoticeDocumentUpdateModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplication = processingContext.getPwaApplication();
    var publicNoticeDocumentFileView = publicNoticeService.getLatestPublicNoticeDocumentFileView(pwaApplication);
    var publicNoticeOverviewUrl = ReverseRouter.route(on(PublicNoticeOverviewController.class).renderPublicNoticeOverview(
        pwaApplication.getId(), pwaApplication.getApplicationType(), null, null));

    var modelAndView = new ModelAndView("publicNotice/requestPublicNoticeDocumentUpdate")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("publicNoticeDocumentFileView", publicNoticeDocumentFileView)
        .addObject("cancelUrl", publicNoticeOverviewUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Request update for public notice document");
    return modelAndView;
  }





}