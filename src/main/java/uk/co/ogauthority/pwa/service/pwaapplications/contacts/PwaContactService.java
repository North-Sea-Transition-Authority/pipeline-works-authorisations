package uk.co.ogauthority.pwa.service.pwaapplications.contacts;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.masterpwas.contacts.PwaContactRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;

/**
 * Service to administer PWA application-scoped teams (known as contacts).
 */
@Service
public class PwaContactService {

  private final PwaContactRepository pwaContactRepository;

  @Autowired
  public PwaContactService(PwaContactRepository pwaContactRepository) {
    this.pwaContactRepository = pwaContactRepository;
  }

  public List<PwaContact> getContactsForPwaApplication(PwaApplication pwaApplication) {
    return pwaContactRepository.findAllByPwaApplication(pwaApplication);
  }

  @Transactional
  public void addContact(PwaApplication pwaApplication, Person person, Set<PwaContactRole> roles) {
    var contact = new PwaContact(pwaApplication, person, roles);
    pwaContactRepository.save(contact);
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
            String.format("Couldn't find contact for pwa application ID: %s and person ID: %s", pwaApplication.getId(), person.getId())));
  }

  public boolean personHasContactRoleForPwaApplication(PwaApplication pwaApplication, Person person, PwaContactRole role) {
    return getContact(pwaApplication, person)
        .map(contact -> contact.getRoles().contains(role))
        .orElse(false);
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

  }

  private long getNumberOfAccessManagersForApplication(PwaApplication pwaApplication) {
    return pwaContactRepository.findAllByPwaApplication(pwaApplication).stream()
        .flatMap(c -> c.getRoles().stream())
        .filter(r -> r.equals(PwaContactRole.ACCESS_MANAGER))
        .count();
  }

  @Transactional
  void updateContactRoles(PwaContact contact, Set<PwaContactRole> roles) {

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
   * @param pwaApplication contacts being updated for
   * @param person being added to contacts/whose roles are being updated
   * @param roles new roles for person
   */
  public void updateContact(PwaApplication pwaApplication, Person person, Set<PwaContactRole> roles) {
    getContact(pwaApplication, person).ifPresentOrElse(
        contact -> updateContactRoles(contact, roles),
        () -> addContact(pwaApplication, person, roles)
    );
  }

  /**
   * Given a {@link PwaContact}, populate and return a {@link TeamMemberView} for use in the generic team management screen.
   */
  public TeamMemberView getTeamMemberView(PwaApplication pwaApplication, PwaContact contact) {

    var applicationId = pwaApplication.getId();
    var person = contact.getPerson();

    var editUrl = ReverseRouter.route(on(PwaContactController.class)
        .renderContactRolesScreen(applicationId, person.getId().asInt(), null, null));
    var removeUrl = ReverseRouter.route(on(PwaContactController.class)
        .renderRemoveContactScreen(applicationId, person.getId().asInt(), null));

    return new TeamMemberView(
        person,
        editUrl,
        removeUrl,
        contact.getRoles().stream()
            .map(r -> new TeamRoleView(r.getRoleName(), r.getRoleName(), r.getRoleName(), r.getDisplayOrder()))
            .collect(Collectors.toSet())
    );

  }
}
