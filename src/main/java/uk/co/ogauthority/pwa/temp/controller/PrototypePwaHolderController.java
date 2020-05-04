package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
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
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@Controller
@RequestMapping("/prototype/pwa-application/{applicationId}")
public class PrototypePwaHolderController {

  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaHolderFormValidator pwaHolderFormValidator;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public PrototypePwaHolderController(TeamService teamService,
                                      PortalOrganisationsAccessor portalOrganisationsAccessor,
                                      PwaApplicationDetailService pwaApplicationDetailService,
                                      PwaHolderFormValidator pwaHolderFormValidator,
                                      PadOrganisationRoleService padOrganisationRoleService) {
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaHolderFormValidator = pwaHolderFormValidator;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  /**
   * Screen allowing user to select the holder for a PWA.
   */
  @GetMapping("/holder")
  public ModelAndView renderHolderScreen(@PathVariable Integer applicationId,
                                         @ModelAttribute("form") PwaHolderForm form,
                                         AuthenticatedUserAccount user) {
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        getHolderModelAndView(user));
  }

  /**
   * Handle storage of holder selected by user.
   */
  @PostMapping("/holder")
  public ModelAndView postHolderScreen(@PathVariable Integer applicationId,
                                       @Valid @ModelAttribute("form") PwaHolderForm form,
                                       BindingResult bindingResult,
                                       AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      pwaHolderFormValidator.validate(form, bindingResult);

      return ControllerUtils.checkErrorsAndRedirect(bindingResult, getHolderModelAndView(user), () -> {

        List<PortalOrganisationUnit> orgUnitsForUser = getOrgUnitsUserCanAccess(user);

        // check that selected org is accessible to user
        PortalOrganisationUnit organisationUnit = portalOrganisationsAccessor.getOrganisationUnitById(form.getHolderOuId())
            .filter(orgUnitsForUser::contains)
            .orElseThrow(() ->
                new PwaEntityNotFoundException(
                    String.format("Couldn't find PortalOrganisationUnit with ID: %s accessible to user with WUA ID: %s",
                        form.getHolderOuId(),
                        user.getWuaId())));

        padOrganisationRoleService.addHolder(detail, organisationUnit);

        // Not using redirect service so it stays for the actual application
        switch (detail.getPwaApplicationType()) {
          case INITIAL:
            // temporary task list
            return ReverseRouter.redirect(on(PrototypePwaApplicationController.class).viewTaskList(detail.getPwaApplication().getId()));
          case CAT_1_VARIATION:
          case CAT_2_VARIATION:
          case DECOMMISSIONING:
          case DEPOSIT_CONSENT:
          case HUOO_VARIATION:
          case OPTIONS_VARIATION:
          default:
            return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));
        }

      });

    });

  }

  private ModelAndView getHolderModelAndView(AuthenticatedUserAccount user) {

    Map<String, String> ouMap = getOrgUnitsUserCanAccess(user).stream()
        .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
        .collect(StreamUtils.toLinkedHashMap(ou -> String.valueOf(ou.getOuId()), PortalOrganisationUnit::getName));

    return new ModelAndView("pwaApplication/temporary/holder")
        .addObject("ouMap", ouMap)
        .addObject("backUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("errorList", List.of());
  }

  private List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {

    List<PortalOrganisationGroup> userOrgGroups = teamService.getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(),
        List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());

    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(userOrgGroups);

  }

}
