package uk.co.ogauthority.pwa.controller.masterpwas.contacts;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.InitialTaskListController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.masterpwas.contacts.AddPwaContactForm;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.AddPwaContactFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/contacts")
public class PwaContactController {

  private final PwaContactService pwaContactService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final TeamManagementService teamManagementService;
  private final AddPwaContactFormValidator addPwaContactFormValidator;

  private final Map<String, String> rolesCheckboxMap;
  private final Map<String, String> allRolesMap;

  @Autowired
  public PwaContactController(PwaContactService pwaContactService,
                              PwaApplicationDetailService pwaApplicationDetailService,
                              ApplicationBreadcrumbService applicationBreadcrumbService,
                              TeamManagementService teamManagementService,
                              AddPwaContactFormValidator addPwaContactFormValidator) {
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.teamManagementService = teamManagementService;
    this.addPwaContactFormValidator = addPwaContactFormValidator;

    rolesCheckboxMap = PwaContactRole.stream()
        .sorted(Comparator.comparing(PwaContactRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaContactRole::getRoleDescription));

    allRolesMap = PwaContactRole.stream()
        .sorted(Comparator.comparing(PwaContactRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(PwaContactRole::getRoleName, PwaContactRole::getRoleDescription));

  }

  @GetMapping
  public ModelAndView renderContactsScreen(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var pwaApplication = detail.getPwaApplication();

      List<TeamMemberView> teamMemberViews = pwaContactService.getContactsForPwaApplication(pwaApplication).stream()
          .map(contact -> pwaContactService.getTeamMemberView(pwaApplication, contact))
          .sorted(Comparator.comparing(TeamMemberView::getFullName))
          .collect(Collectors.toList());

      var modelAndView = new ModelAndView("teamManagement/teamMembers")
          .addObject("teamName", "Application contacts")
          .addObject("teamMemberViews", teamMemberViews)
          .addObject("addUserUrl", ReverseRouter.route(on(PwaContactController.class)
              .renderAddContact(pwaApplication.getApplicationType(), applicationId, null, user)))
          .addObject("showBreadcrumbs", true)
          .addObject("showTopNav", false)
          .addObject("userCanManageAccess", pwaContactService
              .personHasContactRoleForPwaApplication(pwaApplication, user.getLinkedPerson(), PwaContactRole.ACCESS_MANAGER))
          .addObject("allRoles", allRolesMap)
          .addObject("backUrl",
                  ReverseRouter.route(on(InitialTaskListController.class)
                          .viewTaskList(pwaApplication.getId(), null)));

      applicationBreadcrumbService.fromTaskList(pwaApplication, modelAndView, "Application contacts");

      return modelAndView;

    });

  }

  private ModelAndView getAddUserToTeamModelAndView(PwaApplication pwaApplication, AddPwaContactForm form) {

    form.setPwaApplicationId(pwaApplication.getId());

    return new ModelAndView("teamManagement/addUserToTeam")
        .addObject("groupName", pwaApplication.getAppReference() + " contacts")
        .addObject("showTopNav", false)
        .addObject("cancelUrl", ReverseRouter.route(
            on(PwaContactController.class).renderContactsScreen(pwaApplication.getApplicationType(), pwaApplication.getId(), null)))
        .addObject("form", form);
  }

  @GetMapping("/new")
  public ModelAndView renderAddContact(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @ModelAttribute("form") AddPwaContactForm form,
                                       AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        getAddUserToTeamModelAndView(detail.getPwaApplication(), form));

  }

  @PostMapping("/new")
  public ModelAndView addContact(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                 @PathVariable("applicationId") Integer applicationId,
                                 @ModelAttribute("form")  AddPwaContactForm form,
                                 BindingResult bindingResult,
                                 AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var pwaApplication = detail.getPwaApplication();

      form.setPwaApplicationId(pwaApplication.getId());
      addPwaContactFormValidator.validate(form, bindingResult);

      return ControllerUtils.checkErrorsAndRedirect(bindingResult, getAddUserToTeamModelAndView(pwaApplication, form), () -> {

        Optional<Person> person = teamManagementService.getPersonByEmailAddressOrLoginId(form.getUserIdentifier());

        if (person.isEmpty()) {
          return getAddUserToTeamModelAndView(pwaApplication, form); // should never happen, as validator covers this scenario
        }

        return ReverseRouter.redirect(on(PwaContactController.class).renderContactRolesScreen(
            pwaApplication.getApplicationType(),
            applicationId,
            person.get().getId().asInt(),
            null,
            user
        ));

      });

    });

  }

  private ModelAndView getContactRolesModelAndView(PwaApplicationDetail detail, Person person, UserRolesForm form) {

    return new ModelAndView("teamManagement/memberRoles")
        .addObject("teamName", detail.getPwaApplicationRef())
        .addObject("form", form)
        .addObject("roles", rolesCheckboxMap)
        .addObject("userName", person.getFullName())
        .addObject("showTopNav", false)
        .addObject("cancelUrl", ReverseRouter.route(
            on(PwaContactController.class)
                .renderContactsScreen(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null)));

  }

  @GetMapping("{personId}/edit")
  public ModelAndView renderContactRolesScreen(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               @PathVariable("personId") Integer personId,
                                               @ModelAttribute("form") UserRolesForm form,
                                               AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var person = teamManagementService.getPerson(personId);

      // if person is already a contact, pre-populate the form with their roles
      List<String> existingRoles = pwaContactService.getContactRoles(detail.getPwaApplication(), person).stream()
          .map(Enum::name)
          .collect(Collectors.toList());
      form.setUserRoles(existingRoles);

      return getContactRolesModelAndView(detail, person, form);

    });

  }

  @PostMapping("{personId}/edit")
  public ModelAndView updateContactRoles(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("personId") Integer personId,
                                         @ModelAttribute("form") @Valid UserRolesForm form,
                                         BindingResult bindingResult,
                                         AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var person = teamManagementService.getPerson(personId);

      return ControllerUtils.checkErrorsAndRedirect(bindingResult, getContactRolesModelAndView(detail, person, form), () -> {

        Set<PwaContactRole> roles = form.getUserRoles().stream()
            .map(r -> EnumUtils.getEnumValue(PwaContactRole.class, r))
            .collect(Collectors.toSet());

        try {

          pwaContactService.updateContact(detail.getPwaApplication(), person, roles);
          return ReverseRouter.redirect(on(PwaContactController.class)
              .renderContactsScreen(detail.getPwaApplicationType(), applicationId, null));

        } catch (LastAdministratorException e) {

          return getContactRolesModelAndView(detail, person, form)
              .addObject("error",
                  "This person cannot be taken out of the access manager role as they are currently the only person in that role.");

        }

      });

    });

  }

  private ModelAndView getRemoveContactScreenModelAndView(PwaApplicationDetail detail, PwaContact contact) {

    return new ModelAndView("teamManagement/removeMember")
        .addObject("cancelUrl",
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null)))
        .addObject("showTopNav", false)
        .addObject("teamName", detail.getPwaApplicationRef() + " contacts")
        .addObject("teamMember", pwaContactService.getTeamMemberView(detail.getPwaApplication(), contact));

  }

  @GetMapping("{personId}/remove")
  public ModelAndView renderRemoveContactScreen(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                                @PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("personId") Integer personId,
                                                AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var person = teamManagementService.getPerson(personId);
      var contact = pwaContactService.getContactOrError(detail.getPwaApplication(), person);
      return getRemoveContactScreenModelAndView(detail, contact);

    });

  }

  @PostMapping("{personId}/remove")
  public ModelAndView removeContact(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                    @PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("personId") Integer personId,
                                    AuthenticatedUserAccount user) {

    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {

      var person = teamManagementService.getPerson(personId);

      try {

        pwaContactService.removeContact(detail.getPwaApplication(), person);
        return ReverseRouter.redirect(on(PwaContactController.class)
            .renderContactsScreen(detail.getPwaApplicationType(),applicationId, user));

      } catch (LastAdministratorException e) {

        var contact = pwaContactService.getContactOrError(detail.getPwaApplication(), person);
        return getRemoveContactScreenModelAndView(detail, contact)
            .addObject("error",
            "This person cannot be removed from the contacts as they are currently the only person in the access manager role.");

      }

    });

  }

}
