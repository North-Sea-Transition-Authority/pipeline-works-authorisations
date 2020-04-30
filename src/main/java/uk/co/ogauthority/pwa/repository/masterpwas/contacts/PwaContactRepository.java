package uk.co.ogauthority.pwa.repository.masterpwas.contacts;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;



public interface PwaContactRepository  extends CrudRepository<PwaContact, Integer> {

  List<PwaContact> findAllByPwaApplication(PwaApplication pwaApplication);

  Optional<PwaContact> findByPwaApplicationAndPerson(PwaApplication pwaApplication, Person person);

  Long countByPwaApplication(PwaApplication pwaApplication);

}
