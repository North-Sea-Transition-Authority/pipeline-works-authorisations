package uk.co.ogauthority.pwa.features.application.summary.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.co.ogauthority.pwa.controller.appsummary.ApplicationPipelineDataMapGuidanceController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ApplicationDetailNotFoundException;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.model.form.appsummary.PwaApplicationDetailVersionForm;
import uk.co.ogauthority.pwa.model.view.appsummary.VisibleApplicationVersionOptionsForUser;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/summary")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY)
public class ApplicationSummaryController {

  private final ApplicationSummaryViewService applicationSummaryViewService;

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSummaryController.class);

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
    LOGGER.debug(String.format("Got latest app detail with id %s", latestAppDetail.getId()));

    var selectedAppDetailId = applicationDetailId;
    if (applicationDetailId == null) {
      selectedAppDetailId = latestAppDetail.getId();
      form.setApplicationDetailId(latestAppDetail.getId());
    }

    var visibleApplicationVersionOptionsForUser =
        getVisibleAppVersionOptionsForUser(processingContext.getPwaApplication(), authenticatedUserAccount, applicationDetailId);
    LOGGER.debug("visibleApplicationVersionOptionsForUser retrieved");

    var viewAppSummaryUrl  = ReverseRouter.route(on(ApplicationSummaryController.class).renderSummary(
        applicationId, pwaApplicationType, null, null, null, null));

    var appSummaryView = applicationSummaryViewService.getApplicationSummaryViewForAppDetailId(selectedAppDetailId);
    LOGGER.debug("appSummaryView created");

    var caseSummaryView = processingContext.getCaseSummaryView();
    LOGGER.debug("caseSummaryView created");

    var appDetailVersionSearchSelectorItems = visibleApplicationVersionOptionsForUser.getApplicationVersionOptions();
    LOGGER.debug("appDetailVersionSearchSelectorItems created");

    var showVersionSelector = !visibleApplicationVersionOptionsForUser.getApplicationVersionOptions().isEmpty();
    LOGGER.debug("showVersionSelector created");

    return new ModelAndView("pwaApplication/appProcessing/appSummary/viewAppSummary")
        .addObject("appSummaryView", appSummaryView)
        .addObject("caseSummaryView", caseSummaryView)
        .addObject("appDetailVersionSearchSelectorItems", appDetailVersionSearchSelectorItems)
        .addObject("showVersionSelector", showVersionSelector)
        .addObject("viewAppSummaryUrl", viewAppSummaryUrl)
        .addObject("showDiffCheckbox", !PwaApplicationStatus.COMPLETE.equals(latestAppDetail.getStatus()))
        .addObject("mappingGuidanceUrl", ReverseRouter.route(on(ApplicationPipelineDataMapGuidanceController.class)
            .renderMappingGuidance(applicationId, pwaApplicationType, null)));
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

  private VisibleApplicationVersionOptionsForUser getVisibleAppVersionOptionsForUser(PwaApplication pwaApplication,
                                                                                           AuthenticatedUserAccount user,
                                                                                           Integer applicationDetailId) {
    var visibleApplicationVersionOptionsForUser = applicationSummaryViewService
        .getVisibleApplicationVersionOptionsForUser(pwaApplication, user);
    if (Objects.nonNull(applicationDetailId)) {
      checkAppVersionOptionIsAccessible(visibleApplicationVersionOptionsForUser, applicationDetailId);
    }
    return visibleApplicationVersionOptionsForUser;
  }

  private void checkAppVersionOptionIsAccessible(VisibleApplicationVersionOptionsForUser visibleOptionsForUser,
                                                 Integer applicationDetailId) {
    if (!visibleOptionsForUser.isApplicationDetailPresent(applicationDetailId)) {
      throw new ApplicationDetailNotFoundException(String.format("Application detail with id %s could not be found",
          applicationDetailId));
    }
  }

}
