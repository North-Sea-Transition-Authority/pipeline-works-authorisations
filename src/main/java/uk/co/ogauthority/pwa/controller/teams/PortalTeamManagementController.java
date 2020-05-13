package uk.co.ogauthority.pwa.controller.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddUserToTeamForm;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamView;
import uk.co.ogauthority.pwa.model.teams.PwaTeam;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.teammanagement.AddUserToTeamFormValidator;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.util.ControllerUtils;

@Controller
@RequestMapping("/portal-team-management")
public class PortalTeamManagementController {

  private final TeamManagementService teamManagementService;
  private final AddUserToTeamFormValidator addUserToTeamFormValidator;

  @Autowired
  public PortalTeamManagementController(TeamManagementService teamManagementService,
                                        AddUserToTeamFormValidator addUserToTeamFormValidator) {
    this.teamManagementService = teamManagementService;
    this.addUserToTeamFormValidator = addUserToTeamFormValidator;
  }

  /**
   * Display all teams the user can manage.
   * If they can only manage a single team they are redirected to its management page.
   */
  @GetMapping("")
  public ModelAndView renderManageableTeams(AuthenticatedUserAccount currentUser) {
    var modelAndView = new ModelAndView("teamManagement/manageableTeams");

    List<TeamView> teamViews = teamManagementService.getAllPwaTeamsUserCanManage(currentUser).stream()
        .map(team -> new TeamView(team,
            ReverseRouter.route(on(PortalTeamManagementController.class).renderTeamMembers(team.getId(), null)))
        )
        .collect(Collectors.toList());

    if (teamViews.size() > 1) {
      modelAndView.addObject("teamViewList", teamViews);
    } else if (teamViews.size() == 1) {
      // Dont show team list if there's only 1
      return new ModelAndView("redirect:" + teamViews.get(0).getSelectRoute());
    } else {
      throw new AccessDeniedException(String.format(
          "User with wuaId %s cannot manage any teams", currentUser.getWuaId()
      ));
    }
    return modelAndView;
  }

  @GetMapping("/teams/{resId}/member")
  public ModelAndView renderTeamMembers(@PathVariable Integer resId, AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, (this::getTeamUsersModelAndView));
  }

  private ModelAndView getTeamUsersModelAndView(PwaTeam team) {
    List<TeamMemberView> teamMemberViews = teamManagementService.getTeamMemberViewsForTeam(team).stream()
        .sorted(Comparator.comparing(TeamMemberView::getForename).thenComparing(TeamMemberView::getSurname))
        .collect(Collectors.toList());

    return new ModelAndView("teamManagement/teamMembers")
        .addObject("teamId", team.getId())
        .addObject("teamName", team.getName())
        .addObject("teamMemberViews", teamMemberViews)
        .addObject("addUserUrl", ReverseRouter.route(
            on(PortalTeamManagementController.class).renderAddUserToTeam(team.getId(), null, null)
        ))
        .addObject("showBreadcrumbs", false)
        .addObject("userCanManageAccess", true)
        .addObject("showTopNav", true);
  }


  @GetMapping("/teams/{resId}/member/new")
  public ModelAndView renderAddUserToTeam(@PathVariable Integer resId,
                                          @ModelAttribute("form") AddUserToTeamForm userForm,
                                          AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, (this::getAddUserToTeamModelAndView));
  }

  private ModelAndView getAddUserToTeamModelAndView(PwaTeam team) {
    return new ModelAndView("teamManagement/addUserToTeam")
        .addObject("groupName", "team")
        .addObject("teamId", team.getId())
        .addObject("showTopNav", true)
        .addObject("cancelUrl", ReverseRouter.route(
            on(PortalTeamManagementController.class).renderTeamMembers(team.getId(), null))
        );
  }

  @PostMapping("/teams/{resId}/member/new")
  public ModelAndView handleAddUserToTeamSubmit(@PathVariable Integer resId,
                                                @ModelAttribute("form") AddUserToTeamForm userForm,
                                                BindingResult result,
                                                AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, team -> {
      userForm.setResId(resId);
      addUserToTeamFormValidator.validate(userForm, result);

      if (result.hasErrors()) {
        return getAddUserToTeamModelAndView(team);
      } else {
        var person = teamManagementService.getPersonByEmailAddressOrLoginId(userForm.getUserIdentifier())
            .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
                "No person found with email/loginId %s. This should have been caught by form validation.", userForm.getUserIdentifier())
            ));
        return ReverseRouter.redirect(on(PortalTeamManagementController.class)
            .renderMemberRoles(team.getId(), person.getId().asInt(), null, null));
      }
    });
  }

  @GetMapping("/teams/{resId}/member/{personId}/remove")
  public ModelAndView renderRemoveTeamMember(@PathVariable Integer resId,
                                             @PathVariable Integer personId,
                                             AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, team -> {
      var person = teamManagementService.getPerson(personId);
      return getRemoveTeamMemberModelAndView(team, person, null);
    });
  }

  @PostMapping("/teams/{resId}/member/{personId}/remove")
  public ModelAndView handleRemoveTeamMemberSubmit(@PathVariable Integer resId,
                                                   @PathVariable Integer personId,
                                                   AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, team -> {
      var person = teamManagementService.getPerson(personId);
      try {
        teamManagementService.removeTeamMember(person, team, currentUser);
      } catch (LastAdministratorException e) {
        return getRemoveTeamMemberModelAndView(team, person,
            "This person cannot be removed from the team as they are currently the only person in the team administrator role.");
      }
      return ReverseRouter.redirect(on(PortalTeamManagementController.class).renderTeamMembers(resId, null));
    });
  }

  private ModelAndView getRemoveTeamMemberModelAndView(PwaTeam team, Person person, String error) {
    var teamMemberView = teamManagementService.getTeamMemberViewForTeamAndPerson(team, person)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("personId: %s is not a member of resId: %s", person.getId(), team.getId())
        ));

    return new ModelAndView("teamManagement/removeMember")
        .addObject("cancelUrl", ReverseRouter.route(on(PortalTeamManagementController.class).renderTeamMembers(team.getId(), null)))
        .addObject("showTopNav", true)
        .addObject("teamName", team.getName())
        .addObject("teamMember", teamMemberView)
        .addObject("error", error);
  }

  @GetMapping("/teams/{resId}/member/{personId}/roles")
  public ModelAndView renderMemberRoles(@PathVariable Integer resId,
                                        @PathVariable Integer personId,
                                        @ModelAttribute("form") UserRolesForm form,
                                        AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, team -> {
      var person = teamManagementService.getPerson(personId);
      if (form.getUserRoles() == null) {
        teamManagementService.populateExistingRoles(person, team, form);
      }
      return getMemberRolesModelAndView(team, person, form);
    });
  }

  @PostMapping("/teams/{resId}/member/{personId}/roles")
  public ModelAndView handleMemberRolesUpdate(@PathVariable Integer resId,
                                              @PathVariable Integer personId,
                                              @Valid @ModelAttribute("form") UserRolesForm form,
                                              BindingResult result,
                                              AuthenticatedUserAccount currentUser) {
    return withManageableTeam(resId, currentUser, team -> {
      var person = teamManagementService.getPerson(personId);

      if (result.hasErrors()) {
        return getMemberRolesModelAndView(team, person, form);
      }

      try {
        teamManagementService.updateUserRoles(person, team, form, currentUser);
        return ReverseRouter.redirect(on(PortalTeamManagementController.class).renderTeamMembers(resId, null));
      } catch (LastAdministratorException e) {
        result.rejectValue("userRoles", "userRoles.invalid", "You cannot remove the last administrator from a team");
        return getMemberRolesModelAndView(team, person, form);
      }
    });
  }

  private ModelAndView getMemberRolesModelAndView(PwaTeam team, Person person, UserRolesForm form) {
    List<TeamRoleView> roles = teamManagementService.getRolesForTeam(team);
    return new ModelAndView("teamManagement/memberRoles")
        .addObject("teamId", team.getId())
        .addObject("form", form)
        .addObject("roles", ControllerUtils.asCheckboxMap(roles))
        .addObject("teamName", team.getName())
        .addObject("userName", person.getFullName())
        .addObject("showTopNav", true)
        .addObject("cancelUrl", ReverseRouter.route(
            on(PortalTeamManagementController.class).renderTeamMembers(team.getId(), null))
        );
  }

  private ModelAndView withManageableTeam(Integer resId, AuthenticatedUserAccount currentUser, Function<PwaTeam, ModelAndView> function) {
    var team = teamManagementService.getTeamOrError(resId);
    if (teamManagementService.canManageTeam(team, currentUser)) {
      return function.apply(team);
    } else {
      throw new AccessDeniedException(String.format(
          "User with wua id %s attempted to mange resId %s but does not have the correct privs", currentUser.getWuaId(), team.getId()
      ));
    }
  }

}
