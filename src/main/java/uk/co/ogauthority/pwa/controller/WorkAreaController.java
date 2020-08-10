package uk.co.ogauthority.pwa.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartPwaApplicationController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTabService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTabUrlFactory;
import uk.co.ogauthority.pwa.temp.controller.StartPrototypePwaApplicationController;

@Controller
@RequestMapping
public class WorkAreaController {

  private final WorkAreaService workAreaService;
  private final WorkAreaTabService workAreaTabService;

  @Autowired
  public WorkAreaController(WorkAreaService workAreaService,
                            WorkAreaTabService workAreaTabService) {
    this.workAreaService = workAreaService;
    this.workAreaTabService = workAreaTabService;
  }

  /**
   * Figures out which tab to select for user and redirect accordingly.
   */
  @GetMapping("/work-area")
  public ModelAndView renderWorkArea(AuthenticatedUserAccount authenticatedUserAccount) {

    Optional<WorkAreaTab> tab = workAreaTabService.getDefaultTabForPerson(authenticatedUserAccount.getLinkedPerson());

    if (tab.isPresent()) {
      return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkAreaTab(null, tab.get(), null));
    }

    throw new AccessDeniedException(
        String.format("User with login id [%s] cannot access any work area tabs", authenticatedUserAccount.getLoginId()));

  }

  /**
   * Gets the assigned task list for the logged-in user.
   *
   * @return work area screen
   */
  @GetMapping("/work-area/{tabKey}")
  public ModelAndView renderWorkAreaTab(AuthenticatedUserAccount authenticatedUserAccount,
                                        @PathVariable("tabKey") WorkAreaTab tab,
                                        @RequestParam(defaultValue = "0", name = "page") Integer page) {

    var tabs = workAreaTabService.getTabsAvailableToPerson(authenticatedUserAccount.getLinkedPerson());

    if (!tabs.contains(tab)) {
      throw new AccessDeniedException(String.format(
          "User with login id [%s] cannot access %s work area tab",
          authenticatedUserAccount.getLoginId(),
          tab.name()));
    }

    return new ModelAndView("workArea")
        .addObject("prototypeApplicationUrl",
            ReverseRouter.route(on(StartPrototypePwaApplicationController.class).renderStartApplication(null)))
        .addObject("startPwaApplicationUrl",
            ReverseRouter.route(on(StartPwaApplicationController.class).renderStartApplication(null)))
        .addObject("workAreaResult", workAreaService.getWorkAreaResult(authenticatedUserAccount, tab, page))
        .addObject("tabUrlFactory", new WorkAreaTabUrlFactory())
        .addObject("currentWorkAreaTab", tab)
        .addObject("availableTabs", tabs);

  }

}