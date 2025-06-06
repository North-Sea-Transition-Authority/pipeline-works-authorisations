package uk.co.ogauthority.pwa.features.application.creation.controller;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.auth.HasAnyRole;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.creation.ApplicantOrganisationService;
import uk.co.ogauthority.pwa.features.application.creation.PickPwaForm;
import uk.co.ogauthority.pwa.features.application.creation.PickPwaFormValidator;
import uk.co.ogauthority.pwa.features.application.creation.PickedPwaRetrievalService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{resourceType}/pick-pwa-for-application")
@HasAnyRole(teamType = TeamType.ORGANISATION, roles = {Role.APPLICATION_CREATOR})
public class PickExistingPwaController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PickExistingPwaController.class);


  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PickedPwaRetrievalService pickedPwaRetrievalService;
  private final ControllerHelperService controllerHelperService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final PickPwaFormValidator pickPwaFormValidator;
  private final MetricsProvider metricsProvider;
  private final ApplicantOrganisationService applicantOrganisationService;
  private final SystemAreaAccessService systemAreaAccessService;

  @Autowired
  public PickExistingPwaController(
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PickedPwaRetrievalService pickPwaService,
      ControllerHelperService controllerHelperService,
      PwaHolderTeamService pwaHolderTeamService,
      PwaApplicationCreationService pwaApplicationCreationService,
      PickPwaFormValidator pickPwaFormValidator,
      MetricsProvider metricsProvider,
      ApplicantOrganisationService applicantOrganisationService, SystemAreaAccessService systemAreaAccessService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pickedPwaRetrievalService = pickPwaService;
    this.controllerHelperService = controllerHelperService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.pickPwaFormValidator = pickPwaFormValidator;
    this.metricsProvider = metricsProvider;
    this.applicantOrganisationService = applicantOrganisationService;
    this.systemAreaAccessService = systemAreaAccessService;
  }

  @GetMapping
  public ModelAndView renderPickPwaToStartApplication(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                                      @PathVariable @ResourceTypeUrl PwaResourceType resourceType,
                                                      @ModelAttribute("form") PickPwaForm form,
                                                      AuthenticatedUserAccount user) {
    systemAreaAccessService.canStartApplicationOrThrow(user);
    ControllerUtils.startVariationControllerCheckAppType(applicationType);
    return getPickPwaModelAndView(user, applicationType, resourceType);
  }

  private ModelAndView getPickPwaModelAndView(AuthenticatedUserAccount user,
                                              PwaApplicationType pwaApplicationType,
                                              PwaResourceType resourceType) {
    var pickableOptions = pickedPwaRetrievalService.getPickablePwaOptions(user, resourceType);

    List<String> ogList = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
            user,
            Set.of(Role.APPLICATION_CREATOR)
        )
        .stream()
        .sorted(Comparator.comparing(PortalOrganisationGroup::getName))
        .map(PortalOrganisationGroup::getName)
        .collect(Collectors.toList());
    var showNonConsentedOptions = !pickableOptions.getNonconsentedPickablePwas().isEmpty()
        && PwaApplicationType.DEPOSIT_CONSENT.equals(pwaApplicationType);

    return new ModelAndView("pwaApplication/shared/pickPwaForApplication")
        .addObject("consentedPwaMap", pickableOptions.getConsentedPickablePwas())
        .addObject("nonConsentedPwaMap", pickableOptions.getNonconsentedPickablePwas())
        .addObject("backUrl", ReverseRouter.route(on(PwaResourceTypeController.class).renderResourceTypeForm(null, null)))
        .addObject("ogList", ogList)
        .addObject("errorList", List.of())
        .addObject("pwaApplicationType", pwaApplicationType)
        .addObject("showNonConsentedOptions", showNonConsentedOptions);
  }

  @PostMapping
  public ModelAndView pickPwaAndStartApplication(@PathVariable @ApplicationTypeUrl PwaApplicationType applicationType,
                                                 @PathVariable @ResourceTypeUrl PwaResourceType resourceType,
                                                 @ModelAttribute("form") @Valid PickPwaForm form,
                                                 BindingResult bindingResult,
                                                 AuthenticatedUserAccount user) {

    var stopwatch = Stopwatch.createStarted();
    systemAreaAccessService.canStartApplicationOrThrow(user);
    ControllerUtils.startVariationControllerCheckAppType(applicationType);

    pickPwaFormValidator.validate(form, bindingResult, applicationType);
    var modelAndView = controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getPickPwaModelAndView(user, applicationType, resourceType), () -> {

          MasterPwa pickedPwa;
          if (form.getConsentedMasterPwaId() != null) {
            pickedPwa = pickedPwaRetrievalService.getPickedConsentedPwa(form.getConsentedMasterPwaId(), user);
          } else {
            pickedPwa = pickedPwaRetrievalService.getPickedNonConsentedPwa(form.getNonConsentedMasterPwaId(), user);
          }

          var applicantOrganisations = applicantOrganisationService.getPotentialApplicantOrganisations(pickedPwa, user);

          // if there's a single organisation that could be the applicant org, create the app and go to the task list
          if (applicantOrganisations.size() == 1) {

            var newAppDetail = pwaApplicationCreationService
                .createVariationPwaApplication(
                    pickedPwa,
                    applicationType,
                    resourceType,
                    applicantOrganisations.iterator().next(),
                    user
                );

            return pwaApplicationRedirectService.getTaskListRedirect(newAppDetail.getPwaApplication());

          }

          // otherwise make the user pick one
          return ReverseRouter.redirect(on(ApplicantOrganisationController.class)
              .renderSelectOrganisation(pickedPwa.getId(), applicationType, resourceType, null, null));

        });

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getStartAppTimer(), "Variation application started.");

    return modelAndView;

  }

}
