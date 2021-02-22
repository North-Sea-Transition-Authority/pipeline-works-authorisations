package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class RegulatorWorkAreaPageService {

  private final PwaAppProcessingPermissionService appProcessingPermissionService;
  private final WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher;

  @Autowired
  public RegulatorWorkAreaPageService(PwaAppProcessingPermissionService appProcessingPermissionService,
                                      WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher) {

    this.appProcessingPermissionService = appProcessingPermissionService;
    this.workAreaApplicationDetailSearcher = workAreaApplicationDetailSearcher;
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
        on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, page));

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


  private Set<PublicNoticeStatus> getPublicNoticeStatusFilterForUser(AuthenticatedUserAccount user) {

    if (user.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)) {
      return Set.of(PublicNoticeStatus.MANAGER_APPROVAL);

    } else if (user.getUserPrivileges().contains(PwaUserPrivilege.PWA_CASE_OFFICER)) {
      return Set.of(PublicNoticeStatus.DRAFT, PublicNoticeStatus.CASE_OFFICER_REVIEW);
    }

    return Set.of();
  }


  private Page<WorkAreaApplicationDetailSearchItem> getRequiresAttentionPage(AuthenticatedUserAccount userAccount,
                                                                             Set<Integer> applicationIdList,
                                                                             int pageRequest) {

    var searchStatuses = getAdditionalStatusFilterForUser(userAccount);
    var publicNoticeStatuses = getPublicNoticeStatusFilterForUser(userAccount);

    return workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        searchStatuses,
        publicNoticeStatuses,
        applicationIdList
    );

  }

  private Page<WorkAreaApplicationDetailSearchItem> getWaitingOnOthersPage(AuthenticatedUserAccount userAccount,
                                                                           Set<Integer> applicationIdList,
                                                                           int pageRequest) {

    var searchStatuses = getAdditionalStatusFilterForUser(userAccount);

    return workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        searchStatuses,
        applicationIdList
    );

  }

  private String viewApplicationUrlProducer(ApplicationDetailItemView applicationDetailSearchItem) {

    var applicationId = applicationDetailSearchItem.getPwaApplicationId();
    var applicationType = applicationDetailSearchItem.getApplicationType();
    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(applicationId, applicationType, AppProcessingTab.TASKS, null, null));
  }

}
