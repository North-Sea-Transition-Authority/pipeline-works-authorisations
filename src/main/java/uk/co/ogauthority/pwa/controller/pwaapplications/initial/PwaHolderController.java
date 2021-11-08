package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@Controller
@RequestMapping("/pwa-application/create-initial-pwa")
public class PwaHolderController {

  private final TeamService teamService;
  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaHolderFormValidator pwaHolderFormValidator;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final ControllerHelperService controllerHelperService;
  private final String ogaServiceDeskEmail;
  private final MetricsProvider metricsProvider;

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaHolderController.class);


  @Autowired
  public PwaHolderController(TeamService teamService,
                             PwaApplicationCreationService pwaApplicationCreationService,
                             PwaApplicationDetailService pwaApplicationDetailService,
                             PortalOrganisationsAccessor portalOrganisationsAccessor,
                             PwaApplicationRedirectService pwaApplicationRedirectService,
                             PwaHolderFormValidator pwaHolderFormValidator,
                             PadOrganisationRoleService padOrganisationRoleService,
                             ControllerHelperService controllerHelperService,
                             @Value("${oga.servicedesk.email}") String ogaServiceDeskEmail,
                             MetricsProvider metricsProvider) {
    this.teamService = teamService;
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaHolderFormValidator = pwaHolderFormValidator;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.controllerHelperService = controllerHelperService;
    this.ogaServiceDeskEmail = ogaServiceDeskEmail;
    this.metricsProvider = metricsProvider;
  }

  /**
   * Screen allowing user to select the holder for a PWA.
   */
  @GetMapping("/holder")
  public ModelAndView renderHolderScreen(
      @ModelAttribute("form") PwaHolderForm form,
      AuthenticatedUserAccount user) {

    return getHolderModelAndView(user, form);

  }

  /**
   * Handle storage of holder selected by user.
   */
  @PostMapping("/holder")
  public ModelAndView postHolderScreen(
      @Valid @ModelAttribute("form") PwaHolderForm form,
      BindingResult bindingResult,
      AuthenticatedUserAccount user) {

    var stopwatch = Stopwatch.createStarted();
    pwaHolderFormValidator.validate(form, bindingResult);

    var modelAndView =  controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getHolderModelAndView(user, form), () -> {

          List<PortalOrganisationUnit> orgUnitsForUser = getOrgUnitsUserCanAccess(user);

          // check that selected org is accessible to user
          PortalOrganisationUnit organisationUnit = portalOrganisationsAccessor.getOrganisationUnitById(
              form.getHolderOuId())
              .filter(orgUnitsForUser::contains)
              .orElseThrow(() ->
                  new PwaEntityNotFoundException(
                      String.format(
                          "Couldn't find PortalOrganisationUnit with ID: %s accessible to user with WUA ID: %s",
                          form.getHolderOuId(),
                          user.getWuaId())));

          PwaApplication pwaApplication = pwaApplicationCreationService
              .createInitialPwaApplication(organisationUnit, user).getPwaApplication();

          var applicationDetail = pwaApplicationDetailService.getTipDetail(pwaApplication.getId());

          padOrganisationRoleService.addHolder(applicationDetail, organisationUnit);

          return pwaApplicationRedirectService.getTaskListRedirect(pwaApplication);

        });

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getStartAppTimer(), "Initial application started.");

    return modelAndView;
  }

  private ModelAndView getHolderModelAndView(AuthenticatedUserAccount user,
                                             PwaHolderForm form) {

    Map<String, String> ouMap = getOrgUnitsUserCanAccess(user).stream()
        .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
        .collect(StreamUtils.toLinkedHashMap(ou -> String.valueOf(ou.getOuId()), PortalOrganisationUnit::getName));

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
    return teamService.getOrganisationTeamListIfPersonInRole(
        user.getLinkedPerson(),
        List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());
  }

  private List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {
    return portalOrganisationsAccessor.getActiveOrganisationUnitsForOrganisationGroupsIn(getOrgGroupsUserCanAccess(user));
  }

}
