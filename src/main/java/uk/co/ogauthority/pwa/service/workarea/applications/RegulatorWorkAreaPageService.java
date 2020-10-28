package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class RegulatorWorkAreaPageService {

  private final PwaAppProcessingPermissionService appProcessingPermissionService;
  private final ApplicationDetailSearcher applicationDetailSearcher;

  @Autowired
  public RegulatorWorkAreaPageService(PwaAppProcessingPermissionService appProcessingPermissionService,
                                      ApplicationDetailSearcher applicationDetailSearcher) {

    this.appProcessingPermissionService = appProcessingPermissionService;
    this.applicationDetailSearcher = applicationDetailSearcher;
  }

  public PageView<PwaApplicationWorkAreaItem> getRequiresAttentionPageView(
      AuthenticatedUserAccount authenticatedUserAccount,
      Set<Integer> applicationIds,
      int page) {

    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, page));

    return PageView.fromPage(
        getRequiresAttentionPage(authenticatedUserAccount, applicationIds, page),
        workAreaUri,
        sr -> new PwaApplicationWorkAreaItem(sr, this::viewApplicationUrlProducer)
    );

  }

  public PageView<PwaApplicationWorkAreaItem> getWaitingOnOthersPageView(
      AuthenticatedUserAccount authenticatedUserAccount,
      Set<Integer> applicationIds,
      int page) {

    var workAreaUri = ReverseRouter.route(
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, page));

    return PageView.fromPage(
        getWaitingOnOthersPage(authenticatedUserAccount, applicationIds, page),
        workAreaUri,
        sr -> new PwaApplicationWorkAreaItem(sr, this::viewApplicationUrlProducer)
    );

  }

  private Set<PwaApplicationStatus> getAdditionalStatusFilterForUser(AuthenticatedUserAccount user) {

    var searchStatuses = EnumSet.noneOf(PwaApplicationStatus.class);

    var processingPermissions = appProcessingPermissionService.getGenericProcessingPermissions(user);

    if (processingPermissions.contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)) {
      searchStatuses.add(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    }

    return searchStatuses;

  }


  private Page<ApplicationDetailSearchItem> getRequiresAttentionPage(AuthenticatedUserAccount userAccount,
                                                                     Set<Integer> applicationIdList,
                                                                     int pageRequest) {

    var searchStatuses = getAdditionalStatusFilterForUser(userAccount);

    return applicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        searchStatuses,
        applicationIdList
    );

  }

  private Page<ApplicationDetailSearchItem> getWaitingOnOthersPage(AuthenticatedUserAccount userAccount,
                                                                   Set<Integer> applicationIdList,
                                                                   int pageRequest) {

    var searchStatuses = getAdditionalStatusFilterForUser(userAccount);

    return applicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereAnyProcessingWaitFlagTrue(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        searchStatuses,
        applicationIdList
    );

  }

  private String viewApplicationUrlProducer(ApplicationDetailSearchItem applicationDetailSearchItem) {

    var applicationId = applicationDetailSearchItem.getPwaApplicationId();
    var applicationType = applicationDetailSearchItem.getApplicationType();
    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(applicationId, applicationType, AppProcessingTab.TASKS, null, null));
  }

}
