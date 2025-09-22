package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.fivium.energyportal.accounts.starter.EnergyPortalServiceAccessService;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.util.StreamUtil;

/**
 * Service to administer PWA application-scoped teams (known as contacts).
 *
 * <p>TODO split this service into the core app contact service and the service code which supports the task list entry and form.</p>
 */
@Service
public class PwaContactService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PwaContactService.class);

  private final PwaContactRepository pwaContactRepository;
  private final TeamQueryService teamQueryService;
  private final UserAccountService userAccountService;
  private final EnergyPortalServiceAccessService energyPortalServiceAccessService;

  @Autowired
  public PwaContactService(PwaContactRepository pwaContactRepository,
                           TeamQueryService teamQueryService,
                           UserAccountService userAccountService,
                           EnergyPortalServiceAccessService energyPortalServiceAccessService) {
    this.pwaContactRepository = pwaContactRepository;
    this.teamQueryService = teamQueryService;
    this.userAccountService = userAccountService;
    this.energyPortalServiceAccessService = energyPortalServiceAccessService;
  }

  public List<PwaContact> getContactsForPwaApplication(PwaApplication pwaApplication) {
    return pwaContactRepository.findAllByPwaApplication(pwaApplication);
  }

  public List<ContactTeamMemberView> getContactTeamMemberViews(PwaApplication pwaApplication) {
    var pwaContacts = getContactsForPwaApplication(pwaApplication);

    Set<Person> people = pwaContacts.stream()
        .map(PwaContact::getPerson)
        .collect(Collectors.toSet());

    var personToWuaIdMap = userAccountService.getWebUserAccountsByPeople(people)
        .stream()
        .distinct()
        .collect(Collectors.toMap(WebUserAccount::getLinkedPerson, WebUserAccount::getWuaId, StreamUtil.keepFirst()));

    return pwaContacts.stream()
        .flatMap(contact -> {
          var person = contact.getPerson();
          var wuaId = personToWuaIdMap.get(person);
          if (wuaId == null) {
            LOGGER.info("Person {} not found in map of WUA ids. Omitting contact {}", person.getId().asInt(), person.getFullName());
            return Stream.empty();
          }
          return Stream.of(getTeamMemberView(pwaApplication, contact, wuaId));
        })
        .sorted(Comparator.comparing(ContactTeamMemberView::getFullName))
        .collect(Collectors.toList());
  }

  public List<Person> getPeopleInRoleForPwaApplication(PwaApplication pwaApplication, PwaContactRole pwaContactRole) {
    return getContactsForPwaApplication(pwaApplication).stream()
        .filter(contact -> contact.getRoles().contains(pwaContactRole))
        .map(PwaContact::getPerson)
        .toList();
  }

  private void addContact(PwaApplication pwaApplication,
                          WebUserAccount contactUser,
                          Set<PwaContactRole> roles) {
    var isNewUser = !userIsContactOrTeamMember(contactUser);

    var contact = new PwaContact(pwaApplication, contactUser.getLinkedPerson(), roles);
    pwaContactRepository.save(contact);

    if (!isNewUser) {
      return;
    }
    energyPortalServiceAccessService.addUser(contactUser.getWuaId());
  }

  private boolean userIsContactOrTeamMember(WebUserAccount user) {

    var userIsMemberOfAnyTeam = teamQueryService.userIsMemberOfAnyTeam(user.getWuaId());
    var personIsApplicationContact = isPersonApplicationContact(user.getLinkedPerson());

    return personIsApplicationContact || userIsMemberOfAnyTeam;
  }

  public boolean personIsContactOnApplication(PwaApplication pwaApplication, Person person) {
    return getContact(pwaApplication, person).isPresent();
  }

  private Optional<PwaContact> getContact(PwaApplication pwaApplication, Person person) {
    return pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person);
  }

  public PwaContact getContactOrError(PwaApplication pwaApplication, Person person) {
    return getContact(pwaApplication, person)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find contact for pwa application ID: %s and person ID: %s", pwaApplication.getId(),
                person.getId())));
  }

  public Set<PwaContactRole> getContactRoles(PwaApplication pwaApplication, Person person) {
    return getContact(pwaApplication, person)
        .map(PwaContact::getRoles)
        .orElse(Set.of());
  }

  @Transactional
  public void removeContact(PwaApplication pwaApplication, WebUserAccount contactUser) {

    var contact = getContactOrError(pwaApplication, contactUser.getLinkedPerson());
    long numberOfAccessManagers = getNumberOfAccessManagersForApplication(pwaApplication);

    if (contact.getRoles().contains(PwaContactRole.ACCESS_MANAGER) && numberOfAccessManagers == 1) {
      throw new LastAdministratorException("Operation would result in 0 access managers");
    }

    pwaContactRepository.delete(contact);

    var isUserRemovedFromAllTeams = !userIsContactOrTeamMember(contactUser);
    if (!isUserRemovedFromAllTeams) {
      return;
    }
    energyPortalServiceAccessService.removeUser(contactUser.getWuaId());
  }

  private long getNumberOfAccessManagersForApplication(PwaApplication pwaApplication) {
    return pwaContactRepository.findAllByPwaApplication(pwaApplication).stream()
        .flatMap(c -> c.getRoles().stream())
        .filter(r -> r.equals(PwaContactRole.ACCESS_MANAGER))
        .count();
  }

  private void updateContactRoles(PwaContact contact, Set<PwaContactRole> roles) {

    if (roles.isEmpty()) {
      throw new IllegalStateException("Can't update PwaContact when given an empty role set");
    }

    long numberOfAccessManagers = getNumberOfAccessManagersForApplication(contact.getPwaApplication());

    if (contact.getRoles().contains(PwaContactRole.ACCESS_MANAGER)
        && numberOfAccessManagers == 1
        && !roles.contains(PwaContactRole.ACCESS_MANAGER)) {
      throw new LastAdministratorException("Operation would result in 0 access managers");
    }

    contact.setRoles(roles);
    pwaContactRepository.save(contact);

  }

  /**
   * If person is already a contact on the application, update their roles, otherwise add them as a new contact.
   *
   * @param pwaApplication contacts being updated for
   * @param contactUser    being added to contacts/whose roles are being updated
   * @param roles          new roles for person
   */
  @Transactional
  public void updateContact(PwaApplication pwaApplication,
                            WebUserAccount contactUser,
                            Set<PwaContactRole> roles) {
    getContact(pwaApplication, contactUser.getLinkedPerson()).ifPresentOrElse(
        contact -> updateContactRoles(contact, roles),
        () -> addContact(pwaApplication, contactUser, roles)
    );
  }

  /**
   * Given a {@link PwaContact}, populate and return a {@link ContactTeamMemberView} for use in the generic team management screen.
   */
  public ContactTeamMemberView getTeamMemberView(PwaApplication pwaApplication, PwaContact contact, Integer wuaId) {

    var applicationType = pwaApplication.getApplicationType();
    var applicationId = pwaApplication.getId();
    var person = contact.getPerson();

    var editUrl = ReverseRouter.route(on(PwaContactController.class)
        .renderContactRolesScreen(applicationType, applicationId, null, wuaId, null, null));
    var removeUrl = ReverseRouter.route(on(PwaContactController.class)
        .renderRemoveContactScreen(applicationType, applicationId, null, wuaId, null));

    return new ContactTeamMemberView(
        person,
        editUrl,
        removeUrl,
        contact.getRoles().stream()
            .map(r -> new ContactTeamRoleView(r.getRoleName(), r.getRoleName(), r.getRoleName(), r.getDisplayOrder()))
            .collect(Collectors.toSet())
    );

  }

  public Long countContactsByPwaApplication(PwaApplication pwaApplication) {
    return pwaContactRepository.countByPwaApplication(pwaApplication);
  }

  /**
   * get a collection of the Application contact roles for a given webUserAccount where each distinct role is an element.
   */
  public Set<PwaApplicationContactRoleDto> getPwaContactRolesForWebUserAccount(WebUserAccount webUserAccount,
                                                                               Set<PwaContactRole> roleFilter) {
    return getPwaContactRolesForPerson(webUserAccount.getLinkedPerson(), roleFilter);
  }

  /**
   * get a collection of the Application contact roles for a given Person where each distinct role is an element.
   */
  public Set<PwaApplicationContactRoleDto> getPwaContactRolesForPerson(Person person,
                                                                       Set<PwaContactRole> roleFilter) {

    var appContactRoles = new HashSet<PwaApplicationContactRoleDto>();

    var contacts = pwaContactRepository.findAllAsDtoByPerson(person);
    for (PwaContactDto contact : contacts) {
      for (PwaContactRole pwaContactRole : contact.getRoles()) {
        if (roleFilter.contains(pwaContactRole)) {
          appContactRoles.add(new PwaApplicationContactRoleDto(
              contact.getPersonId(),
              contact.getPwaApplicationId(),
              pwaContactRole
          ));
        }
      }
    }

    return appContactRoles;
  }

  public boolean isPersonApplicationContact(Person person) {
    return pwaContactRepository.existsByPerson(person);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return true;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    return bindingResult;
  }

  @Override
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    return List.of(new TaskInfo("Contact", countContactsByPwaApplication(pwaApplicationDetail.getPwaApplication())));
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // Do nothing. Contacts linked per application, not per detail.
  }
}

