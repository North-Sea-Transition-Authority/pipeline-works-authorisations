package uk.co.ogauthority.pwa.integrations.energyportal.people.external;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.internal.PersonRepository;

@Service
public class PersonService {


  private final PersonRepository personRepository;

  @Autowired
  public PersonService(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }


  public List<Person> findAllByIdIn(Collection<PersonId> personIds) {

    return personRepository.findAllByIdIn(
        personIds.stream()
            .map(PersonId::asInt)
            .collect(Collectors.toList())
    );

  }

  public SimplePersonView getSimplePersonView(PersonId personId) {

    var person = getPersonById(personId);
    return mapToSimplePersonView(person);
  }

  private SimplePersonView mapToSimplePersonView(Person person) {
    return new SimplePersonView(
        person.getId(), person.getFullName(), person.getEmailAddress()
    );
  }

  public Person getPersonById(PersonId personId) {

    return personRepository.findById(personId.asInt())
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Person with Id:%s not found", personId.asInt()))
        );

  }

  public Person getPersonById(Integer personId) {

    return personRepository.findById(personId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Person with Id:%s not found", personId))
        );

  }
}
