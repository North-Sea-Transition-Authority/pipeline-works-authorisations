package uk.co.ogauthority.pwa.integrations.energyportal.people.internal;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

public interface PersonRepository extends CrudRepository<Person, Integer> {

  List<Person> findAllByIdIn(Collection<Integer> personIds);


}