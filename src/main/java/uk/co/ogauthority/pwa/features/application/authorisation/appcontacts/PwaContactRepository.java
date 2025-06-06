package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;


public interface PwaContactRepository extends CrudRepository<PwaContact, Integer>, PwaContactDtoRepository {

  List<PwaContact> findAllByPwaApplication(PwaApplication pwaApplication);

  Optional<PwaContact> findByPwaApplicationAndPerson(PwaApplication pwaApplication, Person person);

  Long countByPwaApplication(PwaApplication pwaApplication);

  Boolean existsByPerson(Person person);

}
