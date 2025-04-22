package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasAnyRole;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.ApplicationTypeUtils;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;

@Controller
@RequestMapping("/pwa-application/{resourceType}/new")
@HasAnyRole(teamType = TeamType.ORGANISATION, roles = {Role.APPLICATION_CREATOR})
public class StartInitialPwaController {


  private final SystemAreaAccessService systemAreaAccessService;

  public StartInitialPwaController(SystemAreaAccessService systemAreaAccessService) {
    this.systemAreaAccessService = systemAreaAccessService;
  }

  /**
   * Render of start page for initial PWA application.
   */
  @GetMapping
  public ModelAndView renderStartPage(AuthenticatedUserAccount user, @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    systemAreaAccessService.canStartApplicationOrThrow(user);

    var guideText = "All new projects irrespective of pipeline lengths. ";
    guideText += "This requires a 28 day Public Notice. This also includes cases where there are Median Line implications.";

    return new ModelAndView("pwaApplication/startPages/initial")
        .addObject("startUrl", ReverseRouter.route(on(StartInitialPwaController.class).startInitialPwa(null, resourceType)))
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(PwaApplicationType.INITIAL))
        .addObject("formattedMedianLineDuration",
            ApplicationTypeUtils.getFormattedMedianLineDuration(PwaApplicationType.INITIAL))
        .addObject("resourceTypeGuideText", guideText);
  }

  /**
   * Create initial PWA application and redirect to first task.
   */
  @PostMapping
  public ModelAndView startInitialPwa(AuthenticatedUserAccount user,
                                      @ResourceTypeUrl PwaResourceType resourceType) {
    systemAreaAccessService.canStartApplicationOrThrow(user);
    return ReverseRouter.redirect(on(PwaHolderController.class).renderHolderScreen(null, resourceType, null));
  }

}
