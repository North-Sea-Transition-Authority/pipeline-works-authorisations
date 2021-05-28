package uk.co.ogauthority.pwa.controller.appsummary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.model.form.appsummary.PwaApplicationDetailVersionForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/summary")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY)
public class ApplicationSummaryController {

  private final ApplicationSummaryViewService applicationSummaryViewService;

  @Autowired
  public ApplicationSummaryController(ApplicationSummaryViewService applicationSummaryViewService) {
    this.applicationSummaryViewService = applicationSummaryViewService;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    PwaAppProcessingContext processingContext,
                                    AuthenticatedUserAccount authenticatedUserAccount,
                                    @ModelAttribute("form") PwaApplicationDetailVersionForm form,
                                    @RequestParam(value = "applicationDetailId", required = false) Integer applicationDetailId) {


    var latestAppDetail = processingContext.getApplicationDetail();
    var selectedAppDetailId = applicationDetailId;
    if (applicationDetailId == null) {
      selectedAppDetailId = latestAppDetail.getId();
      form.setApplicationDetailId(latestAppDetail.getId());
    }

    var appDetailVersionSearchSelectorItems = applicationSummaryViewService.getAppDetailVersionSearchSelectorItems(
        processingContext.getPwaApplication());

    var viewAppSummaryUrl  = ReverseRouter.route(on(ApplicationSummaryController.class).renderSummary(
        applicationId, pwaApplicationType, null, null, null, null));

    return new ModelAndView("pwaApplication/appProcessing/appSummary/viewAppSummary")
        .addObject("appSummaryView", applicationSummaryViewService.getApplicationSummaryViewForId(selectedAppDetailId))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("appDetailVersionSearchSelectorItems", appDetailVersionSearchSelectorItems)
        .addObject("showVersionSelector", true)
        .addObject("viewAppSummaryUrl", viewAppSummaryUrl)
        .addObject("showDiffCheckbox", !PwaApplicationStatus.COMPLETE.equals(latestAppDetail.getStatus()));
  }


  @PostMapping()
  public ModelAndView postViewSummary(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           @ModelAttribute("form") PwaApplicationDetailVersionForm form) {

    return ReverseRouter.redirect(on(ApplicationSummaryController.class)
        .renderSummary(applicationId, pwaApplicationType, null, null, null, form.getApplicationDetailId()));
  }

}
