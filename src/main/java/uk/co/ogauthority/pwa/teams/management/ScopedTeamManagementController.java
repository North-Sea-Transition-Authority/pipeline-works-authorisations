package uk.co.ogauthority.pwa.teams.management;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.ogauthority.pwa.fds.searchselector.SearchSelectorResults;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.access.InvokingUserHasStaticRole;
import uk.co.ogauthority.pwa.teams.management.form.NewConsulteeGroupTeamForm;
import uk.co.ogauthority.pwa.teams.management.form.NewConsulteeGroupTeamFormValidator;
import uk.co.ogauthority.pwa.teams.management.form.NewOrganisationTeamForm;
import uk.co.ogauthority.pwa.teams.management.form.NewOrganisationTeamFormValidator;

@Controller
public class ScopedTeamManagementController {

  private final TeamManagementService teamManagementService;
  private final OrganisationApi organisationApi;
  private final NewOrganisationTeamFormValidator newOrganisationTeamFormValidator;
  private final NewConsulteeGroupTeamFormValidator newConsulteeGroupTeamFormValidator;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;

  public ScopedTeamManagementController(TeamManagementService teamManagementService,
                                        OrganisationApi organisationApi,
                                        NewOrganisationTeamFormValidator newOrganisationTeamFormValidator,
                                        NewConsulteeGroupTeamFormValidator newConsulteeGroupTeamFormValidator,
                                        ConsulteeGroupDetailService consulteeGroupDetailService) {
    this.teamManagementService = teamManagementService;
    this.organisationApi = organisationApi;
    this.newOrganisationTeamFormValidator = newOrganisationTeamFormValidator;
    this.newConsulteeGroupTeamFormValidator = newConsulteeGroupTeamFormValidator;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
  }

  // Add one of these get/post handlers for every scoped team time you want users to be able to create themselves.
  // only this creation logic needs to be added, once team is created normal TeamManagementController can be used.

  // Scope type: Organisation
  @GetMapping("/team-management/organisation/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.ORGANISATION_MANAGER)
  public ModelAndView renderCreateNewOrgTeam(@ModelAttribute("form") NewOrganisationTeamForm form) {
    return getCreateOrgModelAndView();
  }

  @PostMapping("/team-management/organisation/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.ORGANISATION_MANAGER)
  public ModelAndView handleCreateNewOrgTeam(@ModelAttribute("form") NewOrganisationTeamForm form, BindingResult bindingResult) {
    if (!newOrganisationTeamFormValidator.isValid(form, bindingResult)) {
      return getCreateOrgModelAndView();
    }

    var projection = new OrganisationGroupProjectionRoot()
        .organisationGroupId()
        .name();

    var organisationGroup = organisationApi.findOrganisationGroup(
          Integer.parseInt(form.getOrgGroupId()),
          projection,
          new RequestPurpose("Find org group to create team"))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Org group with id %s not found".formatted(form.getOrgGroupId())));

    var teamType = TeamType.ORGANISATION;
    var scopeRef = TeamScopeReference.from(organisationGroup.getOrganisationGroupId(), teamType);
    var team = teamManagementService.createScopedTeam(organisationGroup.getName(), teamType, scopeRef);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  @GetMapping("/team-management/organisation/search")
  @ResponseBody
  public Object searchOrganisation(@RequestParam("term") String searchTerm) {
    var projection = new OrganisationGroupsProjectionRoot()
        .organisationGroupId()
        .name();

    var requestPurpose = new RequestPurpose("Find org group to create team");
    var selectorResults = organisationApi.searchOrganisationGroups(searchTerm, projection, requestPurpose).stream()
        .sorted(Comparator.comparing(OrganisationGroup::getName, String.CASE_INSENSITIVE_ORDER))
        .map(organisationGroup ->
            new SearchSelectorResults.Result(organisationGroup.getOrganisationGroupId().toString(), organisationGroup.getName()))
        .toList();

    return new SearchSelectorResults(selectorResults);
  }

  private ModelAndView getCreateOrgModelAndView() {
    return new ModelAndView("teamManagement/scoped/createOrganisationTeam")
        .addObject(
            "organisationSearchUrl",
            StringUtils.stripEnd(
                ReverseRouter.route(on(ScopedTeamManagementController.class).searchOrganisation(null)),
                "?term"));
  }

  // Scope type: Consultee group
  @GetMapping("/team-management/consultee-group/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.CONSULTEE_GROUP_MANAGER)
  public ModelAndView renderCreateNewConsulteeGroupTeam(@ModelAttribute("form") NewConsulteeGroupTeamForm form) {
    return getCreateConsulteeGroupModelAndView();
  }

  @PostMapping("/team-management/consultee-group/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.CONSULTEE_GROUP_MANAGER)
  public ModelAndView handleCreateNewConsulteeGroupTeam(@ModelAttribute("form") NewConsulteeGroupTeamForm form,
                                                        BindingResult bindingResult) {
    if (!newConsulteeGroupTeamFormValidator.isValid(form, bindingResult)) {
      return getCreateConsulteeGroupModelAndView();
    }

    var consulteeGroup = consulteeGroupDetailService.getConsulteeGroupDetailById(Integer.parseInt(form.getConsulteeGroupId()));

    var teamType = TeamType.CONSULTEE;
    var scopeRef = TeamScopeReference.from(consulteeGroup.getConsulteeGroupId().toString(), teamType);
    var team = teamManagementService.createScopedTeam(consulteeGroup.getName(), teamType, scopeRef);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  @GetMapping("/team-management/consultee-group/search")
  @ResponseBody
  public Object searchConsulteeGroup(@RequestParam("term") String searchTerm) {

    var selectorResults = consulteeGroupDetailService.searchConsulteeGroups(searchTerm);

    return new SearchSelectorResults(selectorResults);
  }

  private ModelAndView getCreateConsulteeGroupModelAndView() {
    return new ModelAndView("teamManagement/scoped/createConsulteeGroupTeam")
        .addObject(
            "consulteeGroupSearchUrl",
            StringUtils.stripEnd(
                ReverseRouter.route(on(ScopedTeamManagementController.class).searchConsulteeGroup(null)),
                "?term"));
  }

}
