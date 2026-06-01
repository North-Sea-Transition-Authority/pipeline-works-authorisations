package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasAnyRole;
import uk.co.ogauthority.pwa.config.Profile;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.creation.ApplicationTypeUtils;
import uk.co.ogauthority.pwa.features.application.creation.MedianLineImplication;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{resourceType}/variation/new")
@HasAnyRole(teamType = TeamType.ORGANISATION, roles = {Role.APPLICATION_CREATOR})
public class StartVariationController {

  private final SystemAreaAccessService systemAreaAccessService;
  private final boolean pipelineRecordManagementEnabled;


  public StartVariationController(SystemAreaAccessService systemAreaAccessService,
                                  Environment environment) {
    this.systemAreaAccessService = systemAreaAccessService;
    this.pipelineRecordManagementEnabled = environment.matchesProfiles(Profile.ENABLE_PRUAT_ENHANCEMENTS);
  }

  @GetMapping
  public ModelAndView renderVariationTypeStartPage(AuthenticatedUserAccount user,
                                                   @PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                                   @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    ModelAndView modelAndView;

    systemAreaAccessService.canStartApplicationOrThrow(user);
    checkApplicationResourceType(applicationType, resourceType);
    checkPipelineRecordManagementEnabled(applicationType);

    switch (applicationType) {
      case CAT_1_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/category1");
        break;
      case CAT_2_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/category2");
        break;
      case PIPELINE_RECORD_MANAGEMENT:
        modelAndView = new ModelAndView("pwaApplication/startPages/pipelineRecordManagement");
        break;
      case HUOO_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/huooVariation");
        break;
      case DEPOSIT_CONSENT:
        modelAndView = new ModelAndView("pwaApplication/startPages/depositConsent");
        break;
      case OPTIONS_VARIATION:
        modelAndView = new ModelAndView("pwaApplication/startPages/optionsVariation");
        break;
      case DECOMMISSIONING:
        modelAndView = new ModelAndView("pwaApplication/startPages/decommissioningVariation");
        break;
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", applicationType));
    }

    if (applicationType.getMedianLineImplication().equals(MedianLineImplication.TRUE)) {
      modelAndView.addObject("formattedMedianLineDuration",
          ApplicationTypeUtils.getFormattedMedianLineDuration(applicationType));
    }

    return modelAndView
        .addObject("htmlTitle", "Start new " + applicationType.getDisplayName())
        .addObject("pageHeading", "Start new " + applicationType.getDisplayName())
        .addObject("typeDisplay", applicationType.getDisplayName())
        .addObject("formattedDuration", ApplicationTypeUtils.getFormattedDuration(applicationType))
        .addObject("buttonUrl", ReverseRouter.route(on(this.getClass()).startVariation(user, applicationType, resourceType)));
  }

  @PostMapping
  public ModelAndView startVariation(AuthenticatedUserAccount user,
                                     @PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                     @PathVariable @ResourceTypeUrl PwaResourceType resourceType) {
    systemAreaAccessService.canStartApplicationOrThrow(user);
    checkApplicationResourceType(applicationType, resourceType);
    checkPipelineRecordManagementEnabled(applicationType);

    switch (applicationType) {
      case CAT_1_VARIATION:
      case HUOO_VARIATION:
      case CAT_2_VARIATION:
      case PIPELINE_RECORD_MANAGEMENT:
      case DEPOSIT_CONSENT:
      case OPTIONS_VARIATION:
      case DECOMMISSIONING:
        return ReverseRouter.redirect(on(PickExistingPwaController.class)
            .renderPickPwaToStartApplication(applicationType, resourceType, null, user));
      default:
        throw new AccessDeniedException(String.format("Application type not supported %s", applicationType));
    }
  }

  private void checkApplicationResourceType(PwaApplicationType applicationType, PwaResourceType resourceType) {
    if (!resourceType.getPermittedApplicationTypes().contains(applicationType)) {
      throw new AccessDeniedException(String.format("Application type %s not supported for resource type %s",
          applicationType, resourceType));
    }
  }

  private void checkPipelineRecordManagementEnabled(PwaApplicationType applicationType) {
    if (!pipelineRecordManagementEnabled && applicationType == PwaApplicationType.PIPELINE_RECORD_MANAGEMENT) {
      throw new AccessDeniedException(
          "Pipeline record management applications are not currently available.");
    }
  }
}
