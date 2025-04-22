package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ResourceTypeUrl;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@Controller
@RequestMapping("/pwa-application/create-initial-pwa")
@HasAnyRole(teamType = TeamType.ORGANISATION, roles = {Role.APPLICATION_CREATOR})
public class PwaHolderController {

  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaHolderFormValidator pwaHolderFormValidator;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final ControllerHelperService controllerHelperService;
  private final String ogaServiceDeskEmail;
  private final MetricsProvider metricsProvider;
  private final PwaHolderTeamService pwaHolderTeamService;

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaHolderController.class);
  private final SystemAreaAccessService systemAreaAccessService;


  @Autowired
  public PwaHolderController(PwaApplicationCreationService pwaApplicationCreationService,
                             PwaApplicationDetailService pwaApplicationDetailService,
                             PortalOrganisationsAccessor portalOrganisationsAccessor,
                             PwaApplicationRedirectService pwaApplicationRedirectService,
                             PwaHolderFormValidator pwaHolderFormValidator,
                             PadOrganisationRoleService padOrganisationRoleService,
                             ControllerHelperService controllerHelperService,
                             @Value("${oga.servicedesk.email}") String ogaServiceDeskEmail,
                             MetricsProvider metricsProvider,
                             PwaHolderTeamService pwaHolderTeamService, SystemAreaAccessService systemAreaAccessService) {
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaHolderFormValidator = pwaHolderFormValidator;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.controllerHelperService = controllerHelperService;
    this.ogaServiceDeskEmail = ogaServiceDeskEmail;
    this.metricsProvider = metricsProvider;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.systemAreaAccessService = systemAreaAccessService;
  }

  /**
   * Screen allowing user to select the holder for a PWA.
   */
  @GetMapping("/{resourceType}/holder")
  public ModelAndView renderHolderScreen(
      @ModelAttribute("form") PwaHolderForm form,
      @PathVariable @ResourceTypeUrl PwaResourceType resourceType,
      AuthenticatedUserAccount user) {
    systemAreaAccessService.canStartApplicationOrThrow(user);

    return getHolderModelAndView(user, form);

  }

  /**
   * Handle storage of holder selected by user.
   */
  @PostMapping("/{resourceType}/holder")
  public ModelAndView postHolderScreen(
      @Valid @ModelAttribute("form") PwaHolderForm form,
      @PathVariable @ResourceTypeUrl PwaResourceType resourceType,
      BindingResult bindingResult,
      AuthenticatedUserAccount user) {

    var stopwatch = Stopwatch.createStarted();
    systemAreaAccessService.canStartApplicationOrThrow(user);

    pwaHolderFormValidator.validate(form, bindingResult);

    var modelAndView =  controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getHolderModelAndView(user, form), () -> {

          List<Integer> orgUnitsForUser = getOrgUnitsUserCanAccess(user).stream()
              .map(PortalOrganisationSearchUnit::getOrgUnitId)
              .collect(Collectors.toList());

          // check that selected org is accessible to user
          PortalOrganisationUnit organisationUnit = portalOrganisationsAccessor
              .getOrganisationUnitById(OrganisationUnitId.fromInt(form.getHolderOuId()))
              .filter(ou -> orgUnitsForUser.contains(ou.getOuId()))
              .orElseThrow(() -> new PwaEntityNotFoundException(
                  String.format(
                      "Couldn't find PortalOrganisationUnit with ID: %s accessible to user with WUA ID: %s",
                      form.getHolderOuId(),
                      user.getWuaId())));

          PwaApplication pwaApplication = pwaApplicationCreationService
              .createInitialPwaApplication(organisationUnit, user, resourceType).getPwaApplication();

          var applicationDetail = pwaApplicationDetailService.getTipDetailByAppId(pwaApplication.getId());

          padOrganisationRoleService.addHolder(applicationDetail, organisationUnit);

          return pwaApplicationRedirectService.getTaskListRedirect(pwaApplication);

        });

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getStartAppTimer(), "Initial application started.");

    return modelAndView;
  }

  private ModelAndView getHolderModelAndView(AuthenticatedUserAccount user,
                                             PwaHolderForm form) {

    Map<String, String> ouMap = getOrgUnitsUserCanAccess(user).stream()
        .sorted(Comparator.comparing(PortalOrganisationSearchUnit::getOrgSearchableUnitName))
        .collect(StreamUtils.toLinkedHashMap(ou ->
            String.valueOf(ou.getOrgUnitId()), PortalOrganisationSearchUnit::getOrgSearchableUnitName));

    List<String> ogList = getOrgGroupsUserCanAccess(user).stream()
            .sorted(Comparator.comparing(PortalOrganisationGroup::getName))
            .map(PortalOrganisationGroup::getName)
            .collect(Collectors.toList());

    return new ModelAndView("pwaApplication/form/holder")
        .addObject("ouMap", ouMap)
        .addObject("ogList", ogList)
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("errorList", List.of())
        .addObject("hasHolderSet", form != null && form.getHolderOuId() != null)
        .addObject("ogaServiceDeskEmail", ogaServiceDeskEmail);
  }

  private List<PortalOrganisationGroup> getOrgGroupsUserCanAccess(AuthenticatedUserAccount user) {
    return pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(user, Set.of(Role.APPLICATION_CREATOR));
  }

  private List<PortalOrganisationSearchUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {
    return portalOrganisationsAccessor.getSearchableOrganisationUnitsForOrganisationGroupsIn(getOrgGroupsUserCanAccess(user));
  }

}
