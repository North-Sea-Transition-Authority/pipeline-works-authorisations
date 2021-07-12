package uk.co.ogauthority.pwa.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartPwaApplicationController;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContext;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContextService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTabUrlFactory;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;

@Controller
@RequestMapping
public class WorkAreaController {

  private static final int DEFAULT_PAGE = 0;

  private final WorkAreaService workAreaService;
  private final WorkAreaContextService workAreaContextService;
  private final SystemAreaAccessService systemAreaAccessService;
  private final MetricsProvider metricsProvider;

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkAreaController.class);

  @Autowired
  public WorkAreaController(WorkAreaService workAreaService,
                            WorkAreaContextService workAreaContextService,
                            SystemAreaAccessService systemAreaAccessService,
                            MetricsProvider metricsProvider) {
    this.workAreaService = workAreaService;
    this.workAreaContextService = workAreaContextService;
    this.systemAreaAccessService = systemAreaAccessService;
    this.metricsProvider = metricsProvider;
  }

  /**
   * Figures out which tab to select for user and load accordingly.
   */
  @GetMapping("/work-area")
  public ModelAndView renderWorkArea(HttpServletRequest httpServletRequest,
                                     AuthenticatedUserAccount authenticatedUserAccount,
                                     RedirectAttributes redirectAttributes) {

    var stopwatch = Stopwatch.createStarted();
    var workAreaContext = workAreaContextService.createWorkAreaContext(authenticatedUserAccount);

    var defaultTab = workAreaContext.getDefaultTab()
        .orElseThrow(() -> new AccessDeniedException(
            String.format("User with login id [%s] cannot access any work area tabs",
                authenticatedUserAccount.getLoginId()))
        );

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getWorkAreaTabTimer(), defaultTab.getLabel() + " tab loaded.");

    return getWorkAreaModelAndView(workAreaContext, defaultTab, DEFAULT_PAGE);

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

    var stopwatch = Stopwatch.createStarted();
    var context = workAreaContextService.createWorkAreaContext(authenticatedUserAccount);

    var tabs = context.getSortedUserTabs();

    if (!tabs.contains(tab)) {
      throw new AccessDeniedException(
          String.format(
              "User with wua_id id [%s] cannot access [%s] work area tab",
              context.getWuaId(),
              tab
          )
      );
    }

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getWorkAreaTabTimer(), tab.getLabel() + " tab loaded.");

    return getWorkAreaModelAndView(context, tab, page);

  }

  private ModelAndView getWorkAreaModelAndView(WorkAreaContext workareaContext, WorkAreaTab tab, int page) {

    boolean canStartApps = systemAreaAccessService.canStartApplication(workareaContext.getAuthenticatedUserAccount());

    return new ModelAndView("workArea")
        .addObject("startPwaApplicationUrl",
            ReverseRouter.route(on(StartPwaApplicationController.class).renderStartApplication(null)))
        .addObject("workAreaResult", workAreaService.getWorkAreaResult(workareaContext, tab, page))
        .addObject("tabUrlFactory", new WorkAreaTabUrlFactory())
        .addObject("currentWorkAreaTab", tab)
        .addObject("availableTabs", workareaContext.getSortedUserTabs())
        .addObject("showStartButton", canStartApps);
  }

}