package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
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

  @Autowired
  public WorkAreaService(PwaContactService pwaContactService,
                         ApplicationDetailSearcher applicationDetailSearcher,
                         PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.pwaContactService = pwaContactService;
    this.applicationDetailSearcher = applicationDetailSearcher;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  /**
   * get workarea items for user.
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
    if (userAccount.hasPrivilege(PwaUserPrivilege.PWA_REGULATOR_ADMIN)) {
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
    if (PwaApplicationStatus.DRAFT.equals(applicationDetailSearchItem.getPadStatus())) {
      return pwaApplicationRedirectService.getTaskListRoute(
          applicationDetailSearchItem.getPwaApplicationId(),
          applicationDetailSearchItem.getApplicationType()
      );
    } else {
      return ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null));
    }
  }

}
