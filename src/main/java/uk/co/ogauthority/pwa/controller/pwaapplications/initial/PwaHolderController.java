package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
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
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}")
@PwaApplicationTypeCheck(types = {PwaApplicationType.INITIAL})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PwaHolderController {

  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaHolderFormValidator pwaHolderFormValidator;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final String ogaServiceDeskEmail;


  @Autowired
  public PwaHolderController(TeamService teamService,
                             PortalOrganisationsAccessor portalOrganisationsAccessor,
                             PwaApplicationRedirectService pwaApplicationRedirectService,
                             PwaHolderFormValidator pwaHolderFormValidator,
                             PadOrganisationRoleService padOrganisationRoleService,
                             ApplicationBreadcrumbService breadcrumbService,
                             ControllerHelperService controllerHelperService,
                             @Value("${oga.servicedesk.email}") String ogaServiceDeskEmail) {
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaHolderFormValidator = pwaHolderFormValidator;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.breadcrumbService = breadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.ogaServiceDeskEmail = ogaServiceDeskEmail;
  }

  /**
   * Screen allowing user to select the holder for a PWA.
   */
  @GetMapping("/holder")
  public ModelAndView renderHolderScreen(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable Integer applicationId,
      @ModelAttribute("form") PwaHolderForm form,
      AuthenticatedUserAccount user,
      PwaApplicationContext applicationContext) {

    return getHolderModelAndView(user, applicationContext.getApplicationDetail(), form);

  }

  /**
   * Handle storage of holder selected by user.
   */
  @PostMapping("/holder")
  public ModelAndView postHolderScreen(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable Integer applicationId,
      @Valid @ModelAttribute("form") PwaHolderForm form,
      BindingResult bindingResult,
      AuthenticatedUserAccount user,
      PwaApplicationContext applicationContext) {

    pwaHolderFormValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getHolderModelAndView(user, applicationContext.getApplicationDetail(), form), () -> {

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

          padOrganisationRoleService.addHolder(applicationContext.getApplicationDetail(), organisationUnit);

          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());

        });
  }

  private ModelAndView getHolderModelAndView(AuthenticatedUserAccount user, PwaApplicationDetail detail,
                                             PwaHolderForm form) {

    Map<String, String> ouMap = getOrgUnitsUserCanAccess(user).stream()
        .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
        .collect(StreamUtils.toLinkedHashMap(ou -> String.valueOf(ou.getOuId()), PortalOrganisationUnit::getName));

    List<String> ogList = getOrgGroupsUserCanAccess(user).stream()
            .sorted(Comparator.comparing(PortalOrganisationGroup::getName))
            .map(e -> e.getName())
            .collect(Collectors.toList());

    var modelAndView = new ModelAndView("pwaApplication/form/holder")
        .addObject("ouMap", ouMap)
        .addObject("ogList", ogList)
        .addObject("taskListUrl",
            ReverseRouter.route(
                on(InitialTaskListController.class).viewTaskList(detail.getMasterPwaApplicationId(), null))
        )
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null)))
        .addObject("errorList", List.of())
        .addObject("hasHolderSet", form != null && form.getHolderOuId() != null)
        .addObject("ogaServiceDeskEmail", ogaServiceDeskEmail);

    breadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Consent holder");

    return modelAndView;
  }

  private List<PortalOrganisationGroup> getOrgGroupsUserCanAccess(AuthenticatedUserAccount user) {
    return teamService.getOrganisationTeamListIfPersonInRole(
        user.getLinkedPerson(),
        List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());
  }

  private List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {
    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(getOrgGroupsUserCanAccess(user));
  }

}
