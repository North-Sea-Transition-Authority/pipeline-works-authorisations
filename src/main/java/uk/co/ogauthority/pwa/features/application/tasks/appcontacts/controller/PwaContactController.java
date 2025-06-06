package uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.CASE_OFFICER_REVIEW;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.CONSENT_REVIEW;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.DRAFT;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.UPDATE_REQUESTED;

import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.AddPwaContactFormValidator;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.ContactTeamMemberView;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContact;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.masterpwas.contacts.AddPwaContactForm;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/contacts")
@PwaApplicationStatusCheck(statuses =
    {DRAFT, UPDATE_REQUESTED, INITIAL_SUBMISSION_REVIEW, AWAITING_APPLICATION_PAYMENT, CASE_OFFICER_REVIEW, CONSENT_REVIEW})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.MANAGE_CONTACTS)
public class PwaContactController {

  private static final String APP_USERS_PAGE = "Application users";

  private final PwaContactService pwaContactService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final UserAccountService userAccountService;
  private final PersonService personService;
  private final AddPwaContactFormValidator addPwaContactFormValidator;
  private final ControllerHelperService controllerHelperService;
  private final PwaHolderService pwaHolderService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  private final Map<String, String> rolesCheckboxMap;
  private final Map<String, String> allRolesMap;
  private final String ogaRegistrationLink;

  @Autowired
  public PwaContactController(PwaContactService pwaContactService,
                              ApplicationBreadcrumbService applicationBreadcrumbService,
                              UserAccountService userAccountService,
                              PersonService personService,
                              AddPwaContactFormValidator addPwaContactFormValidator,
                              ControllerHelperService controllerHelperService,
                              PwaHolderService pwaHolderService,
                              PwaApplicationRedirectService pwaApplicationRedirectService,
                              @Value("${oga.registration.link}") String ogaRegistrationLink) {
    this.pwaContactService = pwaContactService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.userAccountService = userAccountService;
    this.personService = personService;
    this.addPwaContactFormValidator = addPwaContactFormValidator;
    this.controllerHelperService = controllerHelperService;
    this.pwaHolderService = pwaHolderService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.ogaRegistrationLink = ogaRegistrationLink;

    rolesCheckboxMap = PwaContactRole.stream()
        .sorted(Comparator.comparing(PwaContactRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaContactRole::getRoleDescription));

    allRolesMap = PwaContactRole.stream()
        .sorted(Comparator.comparing(PwaContactRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(PwaContactRole::getRoleName, PwaContactRole::getRoleDescription));

  }

  @GetMapping
  // allow preparers to see users but not modify
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.MANAGE_CONTACTS, PwaApplicationPermission.EDIT})
  public ModelAndView renderContactsScreen(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           PwaApplicationContext applicationContext,
                                           AuthenticatedUserAccount user) {

    var pwaApplication = applicationContext.getPwaApplication();

    List<ContactTeamMemberView> contactTeamMemberViews = pwaContactService.getContactsForPwaApplication(pwaApplication).stream()
        .map(contact -> pwaContactService.getTeamMemberView(pwaApplication, contact))
        .sorted(Comparator.comparing(ContactTeamMemberView::getFullName))
        .collect(Collectors.toList());

    Set<String> orgGroupHolders = pwaHolderService.getPwaHolderOrgGroups(pwaApplication.getMasterPwa()).stream()
        .map(PortalOrganisationGroup::getName)
        .collect(Collectors.toSet());

    var userCanAccessTaskList = applicationContext.hasPermission(PwaApplicationPermission.EDIT)
        && ApplicationState.INDUSTRY_EDITABLE.includes(applicationContext.getApplicationDetail().getStatus());

    var showCaseManagementLink = applicationContext.hasPermission(PwaApplicationPermission.MANAGE_CONTACTS);

    var modelAndView = new ModelAndView("contactTeam/teamMembers")
        .addObject("teamName", "Application users")
        .addObject("contactTeamMemberViews", contactTeamMemberViews)
        .addObject("addUserUrl", ReverseRouter.route(on(PwaContactController.class)
            .renderAddContact(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null, null)))
        .addObject("showBreadcrumbs", true)
        .addObject("showTopNav", false)
        .addObject("userCanManageAccess",
            applicationContext.getPermissions().contains(PwaApplicationPermission.MANAGE_CONTACTS))
        .addObject("allRoles", allRolesMap)
        .addObject("completeSectionUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplication))
        .addObject("orgGroupHolders", orgGroupHolders)
        .addObject("appUser", true)
        .addObject("userType", UserType.INDUSTRY)
        .addObject("userCanAccessTaskList", userCanAccessTaskList)
        .addObject("showCaseManagementLink", showCaseManagementLink)
        .addObject("caseManagementUrl", CaseManagementUtils.routeCaseManagement(
            applicationContext.getMasterPwaApplicationId(), applicationContext.getApplicationType()));

    // Prioritise task list access over accurate breadcrumbs based on user actions.
    if (userCanAccessTaskList) {
      applicationBreadcrumbService.fromTaskList(pwaApplication, modelAndView, APP_USERS_PAGE);
    } else {
      applicationBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, APP_USERS_PAGE);
    }

    return modelAndView;

  }

  private ModelAndView getAddUserToTeamModelAndView(PwaApplication pwaApplication, AddPwaContactForm form) {

    form.setPwaApplicationId(pwaApplication.getId());

    return new ModelAndView("contactTeam/addUserToTeam")
        .addObject("groupName", pwaApplication.getAppReference() + " contacts")
        .addObject("showTopNav", false)
        .addObject("cancelUrl", ReverseRouter.route(
            on(PwaContactController.class).renderContactsScreen(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)))
        .addObject("form", form)
        .addObject("ogaRegistrationLink", ogaRegistrationLink);
  }

  @GetMapping("/new")
  public ModelAndView renderAddContact(@PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       PwaApplicationContext applicationContext,
                                       @ModelAttribute("form") AddPwaContactForm form,
                                       AuthenticatedUserAccount user) {
    return getAddUserToTeamModelAndView(applicationContext.getPwaApplication(), form);
  }

  @PostMapping("/new")
  public ModelAndView addContact(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                 @PathVariable("applicationId") Integer applicationId,
                                 PwaApplicationContext applicationContext,
                                 @ModelAttribute("form")  AddPwaContactForm form,
                                 BindingResult bindingResult,
                                 AuthenticatedUserAccount user) {

    var pwaApplication = applicationContext.getPwaApplication();

    form.setPwaApplicationId(pwaApplication.getId());
    addPwaContactFormValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getAddUserToTeamModelAndView(pwaApplication, form), () -> {

      Optional<Person> person = userAccountService.getPersonByEmailAddressOrLoginId(form.getUserIdentifier());

      if (person.isEmpty()) {
        return getAddUserToTeamModelAndView(pwaApplication, form); // should never happen, as validator covers this scenario
      }

      return ReverseRouter.redirect(on(PwaContactController.class).renderContactRolesScreen(
          pwaApplication.getApplicationType(),
          applicationId,
          null,
          person.get().getId().asInt(),
          null,
          user
      ));

    });

  }

  private ModelAndView getContactRolesModelAndView(PwaApplicationDetail detail, Person person, UserRolesForm form) {

    return new ModelAndView("contactTeam/memberRoles")
        .addObject("teamName", detail.getPwaApplicationRef())
        .addObject("form", form)
        .addObject("roles", rolesCheckboxMap)
        .addObject("userName", person.getFullName())
        .addObject("showTopNav", false)
        .addObject("cancelUrl", ReverseRouter.route(
            on(PwaContactController.class)
                .renderContactsScreen(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));

  }

  @GetMapping("{personId}/edit")
  public ModelAndView renderContactRolesScreen(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               PwaApplicationContext applicationContext,
                                               @PathVariable("personId") Integer personId,
                                               @ModelAttribute("form") UserRolesForm form,
                                               AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    var person = personService.getPersonById(personId);

    // if person is already a contact, pre-populate the form with their roles
    List<String> existingRoles = pwaContactService.getContactRoles(detail.getPwaApplication(), person).stream()
        .map(Enum::name)
        .collect(Collectors.toList());
    form.setUserRoles(existingRoles);

    return getContactRolesModelAndView(detail, person, form);

  }

  @PostMapping("{personId}/edit")
  public ModelAndView updateContactRoles(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("personId") Integer personId,
                                         PwaApplicationContext applicationContext,
                                         @ModelAttribute("form") @Valid UserRolesForm form,
                                         BindingResult bindingResult,
                                         AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    var person = personService.getPersonById(personId);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getContactRolesModelAndView(detail, person, form), () -> {

      Set<PwaContactRole> roles = form.getUserRoles().stream()
          .map(r -> EnumUtils.getEnumValue(PwaContactRole.class, r))
          .collect(Collectors.toSet());

      try {

        pwaContactService.updateContact(detail.getPwaApplication(), person, roles);
        return ReverseRouter.redirect(on(PwaContactController.class)
            .renderContactsScreen(detail.getPwaApplicationType(), applicationId, null, null));

      } catch (LastAdministratorException e) {

        return getContactRolesModelAndView(detail, person, form)
            .addObject("error",
                "This person cannot be taken out of the access manager role as they are currently the only person in that role.");

      }

    });

  }

  private ModelAndView getRemoveContactScreenModelAndView(PwaApplicationDetail detail, PwaContact contact) {

    return new ModelAndView("contactTeam/removeMember")
        .addObject("cancelUrl",
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)))
        .addObject("showTopNav", false)
        .addObject("teamName", detail.getPwaApplicationRef() + " contacts")
        .addObject("teamMember", pwaContactService.getTeamMemberView(detail.getPwaApplication(), contact));

  }

  @GetMapping("{personId}/remove")
  public ModelAndView renderRemoveContactScreen(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                                @PathVariable("applicationId") Integer applicationId,
                                                PwaApplicationContext applicationContext,
                                                @PathVariable("personId") Integer personId,
                                                AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    var person = personService.getPersonById(personId);
    var contact = pwaContactService.getContactOrError(detail.getPwaApplication(), person);
    return getRemoveContactScreenModelAndView(detail, contact);

  }

  @PostMapping("{personId}/remove")
  public ModelAndView removeContact(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                                    @PathVariable("applicationId") Integer applicationId,
                                    PwaApplicationContext applicationContext,
                                    @PathVariable("personId") Integer personId,
                                    AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    var person = personService.getPersonById(personId);

    try {

      pwaContactService.removeContact(detail.getPwaApplication(), person);
      return ReverseRouter.redirect(on(PwaContactController.class)
          .renderContactsScreen(detail.getPwaApplicationType(), applicationId, null, null));

    } catch (LastAdministratorException e) {

      var contact = pwaContactService.getContactOrError(detail.getPwaApplication(), person);
      return getRemoveContactScreenModelAndView(detail, contact)
          .addObject("error",
          "This person cannot be removed from the contacts as they are currently the only person in the access manager role.");

    }

  }

}
