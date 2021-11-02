package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.List;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

/**
 * Interface used to enhance the default repository so DTOs can be produced easily.
 */
public interface PwaContactDtoRepository {

  List<PwaContactDto> findAllAsDtoByPerson(Person person);
}
