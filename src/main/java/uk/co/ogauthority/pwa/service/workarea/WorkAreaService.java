package uk.co.ogauthority.pwa.service.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
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

  private static final int PAGE_SIZE = 2;

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
  public PageView<PwaApplicationWorkAreaItem> getWorkAreaResultPage(WebUserAccount webUserAccount,
                                                                    WorkAreaTab workAreaTab,
                                                                    int page) {
    var applicationContactRoles = pwaContactService.getPwaContactRolesForWebUserAccount(
        webUserAccount,
        EnumSet.of(PwaContactRole.PREPARER));

    var workAreaUri = ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, workAreaTab, page));
    var resultsPage = applicationDetailSearcher.search(
        PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "padCreatedTimestamp")),
        applicationContactRoles
    );

    return PageView.fromPage(
        resultsPage,
        workAreaUri,
        sr -> new PwaApplicationWorkAreaItem(sr, this::viewApplicationUrlProducer)
    );
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
