package uk.co.ogauthority.pwa.controller.pwaapplications.initial;

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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.validators.PwaHolderFormValidator;

@Controller
@RequestMapping("/pwa-application/initial/{applicationId}")
public class PwaHolderController {

  private final TeamService teamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaHolderFormValidator pwaHolderFormValidator;
  private final ApplicationHolderService applicationHolderService;
  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public PwaHolderController(TeamService teamService,
                             PortalOrganisationsAccessor portalOrganisationsAccessor,
                             PwaApplicationDetailService pwaApplicationDetailService,
                             PwaApplicationRedirectService pwaApplicationRedirectService,
                             PwaHolderFormValidator pwaHolderFormValidator,
                             ApplicationHolderService applicationHolderService,
                             ApplicationBreadcrumbService breadcrumbService) {
    this.teamService = teamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaHolderFormValidator = pwaHolderFormValidator;
    this.applicationHolderService = applicationHolderService;
    this.breadcrumbService = breadcrumbService;
  }

  /**
   * Screen allowing user to select the holder for a PWA.
   */
  @GetMapping("/holder")
  public ModelAndView renderHolderScreen(@PathVariable Integer applicationId,
                                         @ModelAttribute("form") PwaHolderForm form,
                                         AuthenticatedUserAccount user) {


    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
          form.setHolderOuId(applicationHolderService.mapHolderDetailsToForm(detail).getHolderOuId());
          return getHolderModelAndView(user, detail, form);
        }
    );

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

      return ControllerUtils.validateAndRedirect(bindingResult, getHolderModelAndView(user, detail, form), () -> {

        List<PortalOrganisationUnit> orgUnitsForUser = getOrgUnitsUserCanAccess(user);

        // check that selected org is accessible to user
        PortalOrganisationUnit organisationUnit = portalOrganisationsAccessor.getOrganisationUnitById(
            form.getHolderOuId())
            .filter(orgUnitsForUser::contains)
            .orElseThrow(() ->
                new PwaEntityNotFoundException(
                    String.format("Couldn't find PortalOrganisationUnit with ID: %s accessible to user with WUA ID: %s",
                        form.getHolderOuId(),
                        user.getWuaId())));

        applicationHolderService.updateHolderDetails(detail, organisationUnit);

        return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());

      });

    });

  }

  private ModelAndView getHolderModelAndView(AuthenticatedUserAccount user, PwaApplicationDetail applicationDetail,
                                             PwaHolderForm form) {

    Map<String, String> ouMap = getOrgUnitsUserCanAccess(user).stream()
        .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
        .collect(StreamUtils.toLinkedHashMap(ou -> String.valueOf(ou.getOuId()), PortalOrganisationUnit::getName));

    var modelAndView = new ModelAndView("pwaApplication/form/holder")
        .addObject("ouMap", ouMap)
        .addObject("taskListUrl",
            ReverseRouter.route(on(InitialTaskListController.class).viewTaskList(applicationDetail.getMasterPwaApplicationId(), null))
        )
        .addObject("workareaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea()))
        .addObject("errorList", List.of())
        .addObject("hasHolderSet", form != null && form.getHolderOuId() != null);

    breadcrumbService.fromTaskList(applicationDetail.getPwaApplication(), modelAndView, "Consent holder");

    return modelAndView;
  }

  private List<PortalOrganisationUnit> getOrgUnitsUserCanAccess(AuthenticatedUserAccount user) {

    List<PortalOrganisationGroup> userOrgGroups = teamService.getOrganisationTeamListIfPersonInRole(
        user.getLinkedPerson(),
        List.of(PwaOrganisationRole.APPLICATION_CREATOR)).stream()
        .map(PwaOrganisationTeam::getPortalOrganisationGroup)
        .collect(Collectors.toList());

    return portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(userOrgGroups);

  }

}
