package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.service.enums.workarea.WorkAreaFlag;
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

  private Set<PwaApplicationStatus> getAdditionalStatusFilterForUser(Set<PwaAppProcessingPermission> processingPermissions) {

    var searchStatuses = EnumSet.noneOf(PwaApplicationStatus.class);

    if (processingPermissions.contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)) {
      searchStatuses.add(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    }

    if (processingPermissions.contains(PwaAppProcessingPermission.CONSENT_REVIEW)) {
      searchStatuses.add(PwaApplicationStatus.CONSENT_REVIEW);
    }

    return searchStatuses;

  }


  private Set<PublicNoticeStatus> getPublicNoticeStatusFilterForUser(AuthenticatedUserAccount user) {

    if (user.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)) {
      return Set.of(PublicNoticeStatus.MANAGER_APPROVAL);

    } else if (user.getUserPrivileges().contains(PwaUserPrivilege.PWA_CASE_OFFICER)) {
      return Set.of(PublicNoticeStatus.DRAFT);
    }
    return Set.of();
  }

  private boolean getPublicNoticeOverrideFlag(AuthenticatedUserAccount user) {
    return user.getUserPrivileges().contains(PwaUserPrivilege.PWA_MANAGER)
       || user.getUserPrivileges().contains(PwaUserPrivilege.PWA_CASE_OFFICER);
  }


  private Page<WorkAreaApplicationDetailSearchItem> getRequiresAttentionPage(AuthenticatedUserAccount userAccount,
                                                                             Set<Integer> applicationIdList,
                                                                             int pageRequest) {

    var processingPermissions = appProcessingPermissionService.getGenericProcessingPermissions(userAccount);
    var searchStatuses = getAdditionalStatusFilterForUser(processingPermissions);
    var publicNoticeStatuses = getPublicNoticeStatusFilterForUser(userAccount);

    var processingFlagsMap = getProcessingFlagsMapWithDefault(userAccount, processingPermissions, false);

    return workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        searchStatuses,
        publicNoticeStatuses,
        applicationIdList,
        processingFlagsMap
    );

  }

  private Map<WorkAreaFlag, Boolean> getProcessingFlagsMapWithDefault(AuthenticatedUserAccount userAccount,
                                                                      Set<PwaAppProcessingPermission> processingPermissions,
                                                                      Boolean defaultValue) {

    // set all wait flags to default
    var processingFlagsMap = WorkAreaFlag.stream()
        .collect(Collectors.toMap(Function.identity(), val -> defaultValue));

    // get public notice override flag based on user
    boolean publicNoticeOverrideFlag = getPublicNoticeOverrideFlag(userAccount);
    processingFlagsMap.put(WorkAreaFlag.PUBLIC_NOTICE_OVERRIDE, publicNoticeOverrideFlag);

    // then recalculate open consent review flag based on user permissions
    boolean userCanConsentReview = processingPermissions.contains(PwaAppProcessingPermission.CONSENT_REVIEW);
    processingFlagsMap.put(WorkAreaFlag.OPEN_CONSENT_REVIEW_FOREGROUND_FLAG, userCanConsentReview);

    return processingFlagsMap;

  }

  private Page<WorkAreaApplicationDetailSearchItem> getWaitingOnOthersPage(AuthenticatedUserAccount userAccount,
                                                                           Set<Integer> applicationIdList,
                                                                           int pageRequest) {

    var processingPermissions = appProcessingPermissionService.getGenericProcessingPermissions(userAccount);
    var searchStatuses = getAdditionalStatusFilterForUser(processingPermissions);
    var publicNoticeStatuses = getPublicNoticeStatusFilterForUser(userAccount);

    var processingFlagsMap = getProcessingFlagsMapWithDefault(userAccount, processingPermissions, true);

    return workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        searchStatuses,
        publicNoticeStatuses,
        applicationIdList,
        processingFlagsMap
    );

  }

  private String viewApplicationUrlProducer(ApplicationDetailItemView applicationDetailSearchItem) {

    var applicationId = applicationDetailSearchItem.getPwaApplicationId();
    var applicationType = applicationDetailSearchItem.getApplicationType();
    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(applicationId, applicationType, AppProcessingTab.TASKS, null, null));
  }

}
