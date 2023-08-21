package uk.co.ogauthority.pwa.features.appprocessing.casemanagement.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTab;
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTabService;
import uk.co.ogauthority.pwa.features.appprocessing.casemanagement.AppProcessingTabUrlFactory;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.TaskRequirement;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEvent;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventType;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/{tab}")
@PwaAppProcessingPermissionCheck(permissions = {
    PwaAppProcessingPermission.CASE_MANAGEMENT_OGA,
    PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY,
    PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE
})
public class CaseManagementController {

  private final AppProcessingTabService appProcessingTabService;
  private final ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService;
  private final PwaApplicationEventService pwaApplicationEventService;

  @Autowired
  public CaseManagementController(AppProcessingTabService appProcessingTabService,
                                  ConfirmSatisfactoryApplicationService confirmSatisfactoryApplicationService,
                                  PwaApplicationEventService pwaApplicationEventService) {
    this.appProcessingTabService = appProcessingTabService;
    this.confirmSatisfactoryApplicationService = confirmSatisfactoryApplicationService;
    this.pwaApplicationEventService = pwaApplicationEventService;
  }

  @GetMapping
  public ModelAndView renderCaseManagement(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("tab") AppProcessingTab tab,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    var tabs = appProcessingTabService.getTabsAvailableToUser(authenticatedUserAccount);

    if (!tabs.contains(tab)) {
      throw new AccessDeniedException(String.format(
          "User with login id [%s] cannot access %s case management tab",
          authenticatedUserAccount.getLoginId(),
          tab.name()));
    }

    return getCaseManagementModelAndView(processingContext, tab, tabs);

  }

  private ModelAndView getCaseManagementModelAndView(PwaAppProcessingContext appProcessingContext,
                                                     AppProcessingTab currentTab,
                                                     List<AppProcessingTab> availableTabs) {

    var detail = appProcessingContext.getApplicationDetail();

    Map<String, Object> tabContentModelMap = appProcessingTabService.getTabContentModelMap(appProcessingContext, currentTab);

    return new ModelAndView("pwaApplication/appProcessing/caseManagement")
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView())
        .addObject("currentProcessingTab", currentTab)
        .addObject("availableTabs", availableTabs)
        .addObject("tabUrlFactory", new AppProcessingTabUrlFactory(detail))
        .addObject("processingPermissions", appProcessingContext.getAppProcessingPermissions())
        .addObject("taskGroupNameWarningMessageMap",
            getTaskGroupNameWarningMessageMap(appProcessingContext))
        .addAllObjects(tabContentModelMap);
  }

  private Map<String, String> getTaskGroupNameWarningMessageMap(PwaAppProcessingContext appProcessingContext) {

    var taskGroupNameWarningMessageMap = new HashMap<String, String>();

    if (appProcessingContext.getApplicationInvolvement().isUserAssignedCaseOfficer()
        && confirmSatisfactoryApplicationService.confirmSatisfactoryTaskRequired(appProcessingContext.getApplicationDetail())) {
      taskGroupNameWarningMessageMap.put(TaskRequirement.REQUIRED.getDisplayName(),
          "This updated application should be confirmed as satisfactory before performing other tasks.");
    }

    var unclearedConsentIssueFailures = pwaApplicationEventService
        .getUnclearedEventsByApplicationAndType(appProcessingContext.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED);

    if (appProcessingContext.hasProcessingPermission(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA)
        && !unclearedConsentIssueFailures.isEmpty()) {

      var failureInstant = unclearedConsentIssueFailures.stream()
          .max(Comparator.comparing(PwaApplicationEvent::getEventInstant))
          .map(PwaApplicationEvent::getEventInstant)
          .orElseThrow(() -> new IllegalStateException("Couldn't find an uncleared consent issue failure event."));

      taskGroupNameWarningMessageMap.put(TaskRequirement.REQUIRED.getDisplayName(),
          String.format(
              "The consent issue process failed on %s. Contact Support before attempting to issue the consent again.",
              DateUtils.formatDateTime(failureInstant)));

    }

    return taskGroupNameWarningMessageMap;

  }
}
