package uk.co.ogauthority.pwa.controller.appprocessing;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTabService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTabUrlFactory;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/{tab}")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.CASE_MANAGEMENT)
public class CaseManagementController {

  private final AppProcessingTabService appProcessingTabService;

  @Autowired
  public CaseManagementController(AppProcessingTabService appProcessingTabService) {
    this.appProcessingTabService = appProcessingTabService;
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

    Map<String, ?> tabContentModelMap = appProcessingTabService.getTabContentModelMap(appProcessingContext, currentTab);

    return new ModelAndView("pwaApplication/appProcessing/caseManagement")
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView())
        .addObject("currentProcessingTab", currentTab)
        .addObject("availableTabs", availableTabs)
        .addObject("tabUrlFactory", new AppProcessingTabUrlFactory(detail))
        .addObject("processingPermissions", appProcessingContext.getAppProcessingPermissions())
        .addAllObjects(tabContentModelMap);

  }


}