package uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberForm;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberFormValidator;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/consultee-groups")
public class ConsulteeGroupTeamManagementController {

  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final AddConsulteeGroupTeamMemberFormValidator addMemberFormValidator;
  private final TeamManagementService teamManagementService;

  private final Map<String, String> rolesMap;

  @Autowired
  public ConsulteeGroupTeamManagementController(ConsulteeGroupTeamService consulteeGroupTeamService,
                                                AddConsulteeGroupTeamMemberFormValidator addMemberFormValidator,
                                                TeamManagementService teamManagementService) {

    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.addMemberFormValidator = addMemberFormValidator;
    this.teamManagementService = teamManagementService;

    rolesMap = ConsulteeGroupMemberRole.stream()
        .sorted(Comparator.comparing(ConsulteeGroupMemberRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ConsulteeGroupMemberRole::getDescription));

  }

  @GetMapping
  public ModelAndView renderManageableGroups(AuthenticatedUserAccount user) {

    var manageableGroupViews = consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user);

    if (manageableGroupViews.isEmpty()) {
      throw new AccessDeniedException(String.format("User with WUA ID [%s] cannot manage any consultee group teams",
          user.getWuaId()));
    }

    if (manageableGroupViews.size() == 1) {
      return ReverseRouter.redirect(on(ConsulteeGroupTeamManagementController.class)
          .renderTeamMembers(manageableGroupViews.get(0).getConsulteeGroupId(), null));
    }

    return new ModelAndView("pwaApplication/appProcessing/consultations/consultees/manageableGroupTeamList")
        .addObject("consulteeGroupViews", manageableGroupViews);

  }

  @GetMapping("/{consulteeGroupId}/members")
  public ModelAndView renderTeamMembers(@PathVariable Integer consulteeGroupId,
                                        AuthenticatedUserAccount currentUser) {
    return withManageableTeam(consulteeGroupId, currentUser, (this::getTeamUsersModelAndView));
  }

  private ModelAndView getTeamUsersModelAndView(ConsulteeGroupDetail consulteeGroupDetail) {

    List<TeamMemberView> teamMemberViews =
        consulteeGroupTeamService.getTeamMemberViewsForGroup(consulteeGroupDetail.getConsulteeGroup()).stream()
            .sorted(Comparator.comparing(TeamMemberView::getForename).thenComparing(TeamMemberView::getSurname))
            .collect(Collectors.toList());

    return new ModelAndView("teamManagement/teamMembers")
        .addObject("teamId", consulteeGroupDetail.getConsulteeGroupId())
        .addObject("teamName", consulteeGroupDetail.getName())
        .addObject("teamMemberViews", teamMemberViews)
        .addObject("addUserUrl", ReverseRouter.route(
            on(ConsulteeGroupTeamManagementController.class).renderAddUserToTeam(consulteeGroupDetail.getConsulteeGroupId(), null, null)
        ))
        .addObject("showBreadcrumbs", false)
        .addObject("userCanManageAccess", true)
        .addObject("showTopNav", true);

  }

  @GetMapping("/{consulteeGroupId}/members/new")
  public ModelAndView renderAddUserToTeam(@PathVariable Integer consulteeGroupId,
                                          @ModelAttribute("form") AddConsulteeGroupTeamMemberForm form,
                                          AuthenticatedUserAccount user) {
    return withManageableTeam(consulteeGroupId, user, this::getAddUserToTeamModelAndView);
  }

  private ModelAndView getAddUserToTeamModelAndView(ConsulteeGroupDetail groupDetail) {
    return new ModelAndView("teamManagement/addUserToTeam")
        .addObject("groupName", groupDetail.getName())
        .addObject("teamId", groupDetail.getConsulteeGroupId())
        .addObject("showTopNav", true)
        .addObject("cancelUrl", ReverseRouter.route(
            on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(groupDetail.getConsulteeGroupId(), null))
        );
  }

  @PostMapping("/{consulteeGroupId}/members/new")
  public ModelAndView handleAddUserToTeamSubmit(@PathVariable Integer consulteeGroupId,
                                                @ModelAttribute("form") AddConsulteeGroupTeamMemberForm userForm,
                                                BindingResult bindingResult,
                                                AuthenticatedUserAccount currentUser) {

    return withManageableTeam(consulteeGroupId, currentUser, consulteeGroupDetail -> {

      addMemberFormValidator.validate(userForm, bindingResult, consulteeGroupDetail);

      if (bindingResult.hasErrors()) {
        return getAddUserToTeamModelAndView(consulteeGroupDetail);
      } else {

        var person = teamManagementService.getPersonByEmailAddressOrLoginId(userForm.getUserIdentifier())
            .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
                "No person found with email/loginId %s. This should have been caught by form validation.", userForm.getUserIdentifier())
            ));

        return ReverseRouter.redirect(on(ConsulteeGroupTeamManagementController.class)
            .renderMemberRoles(consulteeGroupId, person.getId().asInt(), null, null));

      }
    });

  }

  @GetMapping("/{consulteeGroupId}/member/{personId}/roles")
  public ModelAndView renderMemberRoles(@PathVariable Integer consulteeGroupId,
                                        @PathVariable Integer personId,
                                        @ModelAttribute("form") UserRolesForm form,
                                        AuthenticatedUserAccount currentUser) {
    return withManageableTeam(consulteeGroupId, currentUser, consulteeGroupDetail -> {

      var person = teamManagementService.getPerson(personId);

      // TODO later pre-populate roles

      return getMemberRolesModelAndView(consulteeGroupDetail, person, form);

    });
  }

  @PostMapping("/{consulteeGroupId}/member/{personId}/roles")
  public ModelAndView handleMemberRolesUpdate(@PathVariable Integer consulteeGroupId,
                                              @PathVariable Integer personId,
                                              @Valid @ModelAttribute("form") UserRolesForm form,
                                              BindingResult bindingResult,
                                              AuthenticatedUserAccount currentUser) {
    return withManageableTeam(consulteeGroupId, currentUser, consulteeGroupDetail -> {

      var person = teamManagementService.getPerson(personId);

      if (bindingResult.hasErrors()) {
        return getMemberRolesModelAndView(consulteeGroupDetail, person, form);
      }

      try {
        consulteeGroupTeamService.updateUserRoles(consulteeGroupDetail, person, form, currentUser);
        return ReverseRouter.redirect(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(consulteeGroupId, null));
      } catch (LastAdministratorException e) {
        bindingResult.rejectValue("userRoles", "userRoles.invalid", "You cannot remove the last access manager from a team");
        return getMemberRolesModelAndView(consulteeGroupDetail, person, form);
      }

    });
  }

  private ModelAndView getMemberRolesModelAndView(ConsulteeGroupDetail consulteeGroupDetail, Person person, UserRolesForm form) {

    return new ModelAndView("teamManagement/memberRoles")
        .addObject("teamId", consulteeGroupDetail.getConsulteeGroupId())
        .addObject("form", form)
        .addObject("roles", rolesMap)
        .addObject("teamName", consulteeGroupDetail.getName())
        .addObject("userName", person.getFullName())
        .addObject("showTopNav", true)
        .addObject("cancelUrl", ReverseRouter.route(
            on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(consulteeGroupDetail.getConsulteeGroupId(), null))
        );

  }

  private ModelAndView withManageableTeam(Integer consulteeGroupId,
                                          AuthenticatedUserAccount currentUser,
                                          Function<ConsulteeGroupDetail, ModelAndView> function) {

    return consulteeGroupTeamService.getManageableGroupDetailsForUser(currentUser).stream()
        .filter(groupDetail -> Objects.equals(groupDetail.getConsulteeGroupId(), consulteeGroupId))
        .findFirst()
        .map(function)
        .orElseThrow(() -> new AccessDeniedException(String.format(
          "User with wua id %s attempted to manage consulteeGroup with ID [%s] but does not have the correct privs",
          currentUser.getWuaId(),
          consulteeGroupId
        )));

  }

}
