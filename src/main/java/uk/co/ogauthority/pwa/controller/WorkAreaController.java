package uk.co.ogauthority.pwa.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.features.application.creation.controller.PwaResourceTypeController;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContext;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaContextService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTabCategory;
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
  private final AnalyticsService analyticsService;

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkAreaController.class);

  @Autowired
  public WorkAreaController(WorkAreaService workAreaService,
                            WorkAreaContextService workAreaContextService,
                            SystemAreaAccessService systemAreaAccessService,
                            MetricsProvider metricsProvider,
                            AnalyticsService analyticsService) {
    this.workAreaService = workAreaService;
    this.workAreaContextService = workAreaContextService;
    this.systemAreaAccessService = systemAreaAccessService;
    this.metricsProvider = metricsProvider;
    this.analyticsService = analyticsService;
  }

  /**
   * Figures out which tab to select for user and load accordingly.
   */
  @GetMapping("/work-area")
  public ModelAndView renderWorkArea(HttpServletRequest httpServletRequest,
                                     AuthenticatedUserAccount authenticatedUserAccount,
                                     RedirectAttributes redirectAttributes) {

    checkCanAccessWorkAreaOrThrow(authenticatedUserAccount);

    var workAreaContext = workAreaContextService.createWorkAreaContext(authenticatedUserAccount);

    var defaultTab = workAreaContext.getDefaultTab()
        .orElseThrow(() -> new AccessDeniedException(
            String.format("User with login id [%s] cannot access any work area tabs",
                authenticatedUserAccount.getLoginId()))
        );

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
                                        @RequestParam(defaultValue = "0", name = "page") Integer page,
                                        @CookieValue(name = AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME, required = false)
                                        Optional<String> analyticsClientId) {

    checkCanAccessWorkAreaOrThrow(authenticatedUserAccount);

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

    if (tab.getWorkAreaTabCategory() == WorkAreaTabCategory.BACKGROUND) {
      analyticsService.sendAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.BACKGROUND_WORKAREA_TAB,
          Map.of("tab", tab.getLabel()));
    }

    return getWorkAreaModelAndView(context, tab, page);

  }

  private void checkCanAccessWorkAreaOrThrow(AuthenticatedUserAccount authenticatedUserAccount) {
    if (!systemAreaAccessService.canAccessWorkArea(authenticatedUserAccount)) {
      throw new AccessDeniedException("User %d does not have access to work area".formatted(authenticatedUserAccount.getWuaId()));
    }
  }

  private ModelAndView getWorkAreaModelAndView(WorkAreaContext workareaContext, WorkAreaTab tab, int page) {

    var stopwatch = Stopwatch.createStarted();
    boolean canStartApps = systemAreaAccessService.canStartApplication(workareaContext.getAuthenticatedUserAccount());

    var modelAndView = new ModelAndView("workArea")
        .addObject("startPwaApplicationUrl",
            ReverseRouter.route(on(PwaResourceTypeController.class).renderResourceTypeForm(null, null)))
        .addObject("workAreaResult", workAreaService.getWorkAreaResult(workareaContext, tab, page))
        .addObject("tabUrlFactory", new WorkAreaTabUrlFactory())
        .addObject("currentWorkAreaTab", tab)
        .addObject("availableTabs", workareaContext.getSortedUserTabs())
        .addObject("showStartButton", canStartApps);

    MetricTimerUtils.recordTime(
        stopwatch, LOGGER, metricsProvider.getWorkAreaTabTimer(), tab.getLabel() + " work-area tab processing done.");

    return modelAndView;
  }

}