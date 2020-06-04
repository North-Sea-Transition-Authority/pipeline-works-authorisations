package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.initialreview.InitialReviewController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;

@Service
public class WorkAreaService {

  public static final int PAGE_SIZE = 2;

  private final PwaContactService pwaContactService;
  private final ApplicationDetailSearcher applicationDetailSearcher;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaAppProcessingPermissionService appProcessingPermissionService;

  @Autowired
  public WorkAreaService(PwaContactService pwaContactService,
                         ApplicationDetailSearcher applicationDetailSearcher,
                         PwaApplicationRedirectService pwaApplicationRedirectService,
                         PwaAppProcessingPermissionService appProcessingPermissionService) {
    this.pwaContactService = pwaContactService;
    this.applicationDetailSearcher = applicationDetailSearcher;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.appProcessingPermissionService = appProcessingPermissionService;
  }

  /**
   * Get work area items for user.
   */
  public PageView<PwaApplicationWorkAreaItem> getWorkAreaResultPage(AuthenticatedUserAccount authenticatedUserAccount,
                                                                    WorkAreaTab workAreaTab,
                                                                    int page) {

    var workAreaUri = ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, workAreaTab, page));

    var resultsPage = getUserSearchResults(authenticatedUserAccount, workAreaTab, page);

    return PageView.fromPage(
        resultsPage,
        workAreaUri,
        sr -> new PwaApplicationWorkAreaItem(sr, this::viewApplicationUrlProducer)
    );
  }

  private Page<ApplicationDetailSearchItem> getUserSearchResults(AuthenticatedUserAccount userAccount,
                                                                 WorkAreaTab workAreaTab,
                                                                 int pageRequest) {

    var processingPermissions = appProcessingPermissionService.getProcessingPermissions(userAccount);

    if (processingPermissions.contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)) {

      var adminStatusVisibility = EnumSet.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

      return applicationDetailSearcher.searchByStatus(
          getWorkAreaPageRequest(pageRequest, WorkAreaSort.PROPOSED_DATE_ASC),
          adminStatusVisibility
      );

    } else {

      var applicationContactRoles = pwaContactService.getPwaContactRolesForWebUserAccount(
          userAccount,
          EnumSet.of(PwaContactRole.PREPARER));

      return applicationDetailSearcher.searchByPwaContacts(
          getWorkAreaPageRequest(pageRequest, WorkAreaSort.CREATED_DATE_DESC),
          applicationContactRoles
      );

    }
  }

  private Pageable getWorkAreaPageRequest(int pageRequest, WorkAreaSort workAreaSort) {
    return PageRequest.of(pageRequest, PAGE_SIZE, workAreaSort.getSort());
  }

  private String viewApplicationUrlProducer(ApplicationDetailSearchItem applicationDetailSearchItem) {

    var applicationId = applicationDetailSearchItem.getPwaApplicationId();
    var applicationType = applicationDetailSearchItem.getApplicationType();

    switch (applicationDetailSearchItem.getPadStatus()) {

      case DRAFT:
        return pwaApplicationRedirectService.getTaskListRoute(applicationId, applicationType);
      case INITIAL_SUBMISSION_REVIEW:
        return ReverseRouter.route(on(InitialReviewController.class)
            .renderInitialReview(applicationId, applicationType, null, null, null));
      default:
        return ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null));

    }

  }

}
