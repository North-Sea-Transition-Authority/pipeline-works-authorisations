package uk.co.ogauthority.pwa.features.webapp;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.documents.DocumentTemplateSelectController;
import uk.co.ogauthority.pwa.controller.search.applicationsearch.ApplicationSearchController;
import uk.co.ogauthority.pwa.controller.search.consents.ConsentSearchController;
import uk.co.ogauthority.pwa.controller.teams.ManageTeamsController;
import uk.co.ogauthority.pwa.features.feemanagement.controller.FeeManagementController;
import uk.co.ogauthority.pwa.features.reassignment.CaseReassignmentController;
import uk.co.ogauthority.pwa.model.TopMenuItem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class TopMenuService {

  public static final String WORK_AREA_TITLE = "Work area";
  public static final String TEAM_MANAGEMENT_TITLE = "Manage teams";
  public static final String APPLICATION_SEARCH_TITLE = "Search applications";
  public static final String CONSENT_SEARCH_TITLE = "Search PWAs";
  public static final String TEMPLATE_CLAUSE_MANAGE_TITLE = "Manage template clauses";
  public static final String REASSIGN_CASE_REVIEWS_TITLE = "Reassign Applications";

  public static final String TEMPLATE_FEE_MANAGE_TITLE = "Manage fees";

  private final SystemAreaAccessService systemAreaAccessService;

  @Autowired
  public TopMenuService(SystemAreaAccessService systemAreaAccessService) {
    this.systemAreaAccessService = systemAreaAccessService;
  }

  public List<TopMenuItem> getTopMenuItems(AuthenticatedUserAccount user) {
    List<TopMenuItem> menuItems = new ArrayList<>();

    if (systemAreaAccessService.canAccessWorkArea(user)) {
      menuItems.add(new TopMenuItem(WORK_AREA_TITLE, ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(
          null, null, null))));
    }

    if (systemAreaAccessService.canAccessApplicationSearch(user)) {
      menuItems.add(
          new TopMenuItem(APPLICATION_SEARCH_TITLE, ApplicationSearchController.routeToLandingPage())
      );
    }

    if (systemAreaAccessService.canAccessTeamManagement(user)) {
      menuItems.add(new TopMenuItem(TEAM_MANAGEMENT_TITLE, ReverseRouter.route(on(ManageTeamsController.class)
          .renderTeamTypes(null)))
      );
    }

    if (systemAreaAccessService.canAccessConsentSearch(user)) {
      menuItems.add(new TopMenuItem(CONSENT_SEARCH_TITLE, ReverseRouter.route(on(ConsentSearchController.class)
          .renderSearch(null, null))));
    }

    if (systemAreaAccessService.canAccessTemplateClauseManagement(user)) {
      menuItems.add(new TopMenuItem(TEMPLATE_CLAUSE_MANAGE_TITLE, ReverseRouter.route(on(DocumentTemplateSelectController.class)
          .getTemplatesForSelect(null))));
    }

    if (systemAreaAccessService.canAccessFeePeriodManagement(user)) {
      menuItems.add(new TopMenuItem(TEMPLATE_FEE_MANAGE_TITLE, ReverseRouter.route(on(FeeManagementController.class)
          .renderFeeManagementOverview(null))));
    }

    if (systemAreaAccessService.canAccessTeamManagement(user)) {
      menuItems.add(new TopMenuItem(REASSIGN_CASE_REVIEWS_TITLE, ReverseRouter.route(on(CaseReassignmentController.class)
          .renderCaseReassignment(null, null, null, null))));
    }

    return menuItems;
  }

}
