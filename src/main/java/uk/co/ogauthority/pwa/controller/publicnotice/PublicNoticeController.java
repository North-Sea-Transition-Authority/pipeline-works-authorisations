package uk.co.ogauthority.pwa.controller.publicnotice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.PUBLIC_NOTICE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;

  @Autowired
  public PublicNoticeController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
  }


  @GetMapping
  public ModelAndView renderDraftPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> getDraftPublicNoticeModelAndView(processingContext));
  }



  private ModelAndView getDraftPublicNoticeModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplication = processingContext.getPwaApplication();

    var modelAndView = new ModelAndView("publicNotice/draftPublicNotice")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext));

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Draft a public notice");
    return modelAndView;
  }


}