package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.service.teams.events.NonFoxTeamMemberEventPublisher;

/**
 * Service to administer PWA application-scoped teams (known as contacts).
 *
 * <p>TODO split this service into the core app contact service and the service code which supports the task list entry and form.</p>
 */
@Service
public class PwaContactService implements ApplicationFormSectionService {

  private final PwaContactRepository pwaContactRepository;
  private final NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher;

  @Autowired
  public PwaContactService(PwaContactRepository pwaContactRepository,
                           NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher) {
    this.pwaContactRepository = pwaContactRepository;
    this.nonFoxTeamMemberEventPublisher = nonFoxTeamMemberEventPublisher;
  }

  public List<PwaContact> getContactsForPwaApplication(PwaApplication pwaApplication) {
    return pwaContactRepository.findAllByPwaApplication(pwaApplication);
  }

  public List<Person> getPeopleInRoleForPwaApplication(PwaApplication pwaApplication, PwaContactRole pwaContactRole) {
    return getContactsForPwaApplication(pwaApplication).stream()
        .filter(contact -> contact.getRoles().contains(pwaContactRole))
        .map(PwaContact::getPerson)
        .collect(Collectors.toUnmodifiableList());
  }

  private void addContact(PwaApplication pwaApplication, Person person, Set<PwaContactRole> roles) {
    var contact = new PwaContact(pwaApplication, person, roles);
    pwaContactRepository.save(contact);
    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberAddedEvent(person);
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
  public void removeContact(PwaApplication pwaApplication, Person person) {

    var contact = getContactOrError(pwaApplication, person);
    long numberOfAccessManagers = getNumberOfAccessManagersForApplication(pwaApplication);

    if (contact.getRoles().contains(PwaContactRole.ACCESS_MANAGER) && numberOfAccessManagers == 1) {
      throw new LastAdministratorException("Operation would result in 0 access managers");
    }

    pwaContactRepository.delete(contact);

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberRemovedEvent(person);

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
   * @param person         being added to contacts/whose roles are being updated
   * @param roles          new roles for person
   */
  @Transactional
  public void updateContact(PwaApplication pwaApplication, Person person, Set<PwaContactRole> roles) {
    getContact(pwaApplication, person).ifPresentOrElse(
        contact -> updateContactRoles(contact, roles),
        () -> addContact(pwaApplication, person, roles)
    );
  }

  /**
   * Given a {@link PwaContact}, populate and return a {@link ContactTeamMemberView} for use in the generic team management screen.
   */
  public ContactTeamMemberView getTeamMemberView(PwaApplication pwaApplication, PwaContact contact) {

    var applicationType = pwaApplication.getApplicationType();
    var applicationId = pwaApplication.getId();
    var person = contact.getPerson();

    var editUrl = ReverseRouter.route(on(PwaContactController.class)
        .renderContactRolesScreen(applicationType, applicationId, null, person.getId().asInt(), null, null));
    var removeUrl = ReverseRouter.route(on(PwaContactController.class)
        .renderRemoveContactScreen(applicationType, applicationId, null, person.getId().asInt(), null));

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
    return List.of(new TaskInfo("CONTACT", countContactsByPwaApplication(pwaApplicationDetail.getPwaApplication())));
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // Do nothing. Contacts linked per application, not per detail.
  }
}

