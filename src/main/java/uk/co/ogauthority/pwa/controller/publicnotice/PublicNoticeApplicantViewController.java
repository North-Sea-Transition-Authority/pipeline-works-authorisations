package uk.co.ogauthority.pwa.controller.publicnotice;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/view-public-notice")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_PUBLIC_NOTICE})
public class PublicNoticeApplicantViewController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;

  @Autowired
  public PublicNoticeApplicantViewController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
  }


  private ModelAndView publicNoticeInValidState(PwaAppProcessingContext processingContext,
                                                Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (publicNoticeService.canApplicantViewLatestPublicNotice(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(
              "Access denied as the status of the public notice is not valid in order for applicants to view for application with id: " +
                  processingContext.getMasterPwaApplicationId());
        });
  }


  @GetMapping
  public ModelAndView renderViewPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             PwaAppProcessingContext processingContext,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    return publicNoticeInValidState(processingContext, () ->
        getViewPublicNoticeModelAndView(processingContext));
  }




  private ModelAndView getViewPublicNoticeModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplication = processingContext.getPwaApplication();
    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var publicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);
    var publicNoticeDocumentFileView = publicNoticeService.getLatestPublicNoticeDocumentFileView(pwaApplication);

    var modelAndView = new ModelAndView("publicNotice/viewPublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("coverLetter", publicNoticeRequest.getCoverLetterText())
        .addObject("publicNoticeDocumentFileView", publicNoticeDocumentFileView)
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "View public notice");
    return modelAndView;
  }






}