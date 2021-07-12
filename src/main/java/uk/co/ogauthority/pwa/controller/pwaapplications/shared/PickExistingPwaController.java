package uk.co.ogauthority.pwa.controller.pwaapplications.shared;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartPwaApplicationController;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PickPwaForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pickpwa.PickPwaFormValidator;
import uk.co.ogauthority.pwa.service.pickpwa.PickedPwaRetrievalService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationTypePathUrl}")
public class PickExistingPwaController {

  private static final Set<PwaApplicationType> VALID_START_APPLICATION_TYPES = EnumSet.of(
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(PickExistingPwaController.class);


  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PickedPwaRetrievalService pickedPwaRetrievalService;
  private final ControllerHelperService controllerHelperService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final PickPwaFormValidator pickPwaFormValidator;
  private final MetricsProvider metricsProvider;

  @Autowired
  public PickExistingPwaController(
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PickedPwaRetrievalService pickPwaService,
      ControllerHelperService controllerHelperService,
      PwaHolderTeamService pwaHolderTeamService,
      PwaApplicationCreationService pwaApplicationCreationService,
      PickPwaFormValidator pickPwaFormValidator,
      MetricsProvider metricsProvider) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pickedPwaRetrievalService = pickPwaService;
    this.controllerHelperService = controllerHelperService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.pickPwaFormValidator = pickPwaFormValidator;
    this.metricsProvider = metricsProvider;
  }


  @GetMapping("/pick-pwa-for-application")
  public ModelAndView renderPickPwaToStartApplication(@PathVariable("applicationTypePathUrl")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @ModelAttribute("form") PickPwaForm form,
                                                      AuthenticatedUserAccount user) {
    checkApplicationTypeValid(pwaApplicationType);
    return getPickPwaModelAndView(user, pwaApplicationType);
  }

  private ModelAndView getPickPwaModelAndView(AuthenticatedUserAccount user, PwaApplicationType pwaApplicationType) {
    var pickableOptions = pickedPwaRetrievalService.getPickablePwaOptions(user);

    List<String> ogList = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasOrgRole(user, PwaOrganisationRole.APPLICATION_CREATOR)
        .stream()
        .sorted(Comparator.comparing(PortalOrganisationGroup::getName))
        .map(PortalOrganisationGroup::getName)
        .collect(Collectors.toList());
    var showNonConsentedOptions = !pickableOptions.getNonconsentedPickablePwas().isEmpty()
        && PwaApplicationType.DEPOSIT_CONSENT.equals(pwaApplicationType);

    return new ModelAndView("pwaApplication/shared/pickPwaForApplication")
        .addObject("consentedPwaMap", pickableOptions.getConsentedPickablePwas())
        .addObject("nonConsentedPwaMap", pickableOptions.getNonconsentedPickablePwas())
        .addObject("backUrl", ReverseRouter.route(on(StartPwaApplicationController.class).renderStartApplication(null)))
        .addObject("ogList", ogList)
        .addObject("errorList", List.of())
        .addObject("pwaApplicationType", pwaApplicationType)
        .addObject("showNonConsentedOptions", showNonConsentedOptions);
  }

  @PostMapping("/pick-pwa-for-application")
  public ModelAndView pickPwaAndStartApplication(@PathVariable("applicationTypePathUrl")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @ModelAttribute("form") @Valid PickPwaForm form,
                                                 BindingResult bindingResult,
                                                 AuthenticatedUserAccount user) {

    var stopwatch = Stopwatch.createStarted();
    checkApplicationTypeValid(pwaApplicationType);

    pickPwaFormValidator.validate(form, bindingResult, pwaApplicationType);
    var modelAndView = controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getPickPwaModelAndView(user, pwaApplicationType), () -> {
          MasterPwa pickedPwa;
          if (form.getConsentedMasterPwaId() != null) {
            pickedPwa = pickedPwaRetrievalService.getPickedConsentedPwa(form.getConsentedMasterPwaId(), user);
          } else {
            pickedPwa = pickedPwaRetrievalService.getPickedNonConsentedPwa(form.getNonConsentedMasterPwaId(), user);
          }
          var newApplication = pwaApplicationCreationService.createVariationPwaApplication(
              user,
              pickedPwa,
              pwaApplicationType)
              .getPwaApplication();
          return pwaApplicationRedirectService.getTaskListRedirect(newApplication);
        });

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getStartAppTimer(), "Variation application started.");
    return modelAndView;
  }

  private void checkApplicationTypeValid(PwaApplicationType pwaApplicationType) {
    if (!VALID_START_APPLICATION_TYPES.contains(pwaApplicationType)) {
      throw new AccessDeniedException("Unsupported type for pick pwa: " + pwaApplicationType);
    }
  }


}
