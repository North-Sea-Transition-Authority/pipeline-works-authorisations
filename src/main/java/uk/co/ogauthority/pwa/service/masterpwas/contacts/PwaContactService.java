package uk.co.ogauthority.pwa.service.masterpwas.contacts;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.masterpwas.contacts.PwaContactRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/**
 * Service to administer master PWA-scoped teams (known as contacts).
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

  public boolean personHasContactRoleForPwaApplication(PwaApplication pwaApplication, Person person, PwaContactRole role) {

    return getContact(pwaApplication, person)
        .map(contact -> contact.getRoles().contains(role))
        .orElse(false);

  }
}
