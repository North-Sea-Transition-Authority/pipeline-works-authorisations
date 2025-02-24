package uk.co.ogauthority.pwa.service.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.people.internal.PersonRepository;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

  @Mock
  private PersonRepository personRepository;

  private PersonService personService;

  @Captor
  private ArgumentCaptor<List<Integer>> listCaptor;

  @BeforeEach
  void setUp() {
    personService = new PersonService(personRepository);
  }

  @Test
  void findAllByIdIn() {

    personService.findAllByIdIn(Set.of(new PersonId(1), new PersonId(2)));

    verify(personRepository, times(1)).findAllByIdIn(listCaptor.capture());

    assertThat(listCaptor.getValue()).containsExactlyInAnyOrder(1, 2);

  }

  @Test
  void getPersonById_found() {

    var person = PersonTestUtil.createDefaultPerson();
    when(personRepository.findById(any())).thenReturn(Optional.of(person));

    personService.getPersonById(new PersonId(1));

    verify(personRepository, times(1)).findById(1);

  }

  @Test
  void getPersonById_notFound() {
    when(personRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->

      personService.getPersonById(new PersonId(1)));

  }

}