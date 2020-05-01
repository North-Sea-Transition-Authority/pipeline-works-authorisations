package uk.co.ogauthority.pwa.repository.masterpwas.contacts;

import java.util.List;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;

/**
 * Interface used to enhance the default repository so DTO's can be produced easily.
 */
public interface PwaContactDtoRepository {

  List<PwaContactDto> findAllAsDtoByPerson(Person person);
}
