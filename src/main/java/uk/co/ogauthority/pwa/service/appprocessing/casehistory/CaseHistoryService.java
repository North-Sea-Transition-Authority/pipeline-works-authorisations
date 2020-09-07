package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;

@Service
public class CaseHistoryService {

  private final List<? extends CaseHistoryItemService> itemServices;
  private final PersonRepository personRepository;

  @Autowired
  public CaseHistoryService(List<? extends CaseHistoryItemService> itemServices,
                            PersonRepository personRepository) {
    this.itemServices = itemServices;
    this.personRepository = personRepository;
  }

  public List<CaseHistoryItemView> getCaseHistory(PwaApplication pwaApplication) {

    var caseHistoryItemViews = itemServices.stream()
        .flatMap(itemService -> itemService.getCaseHistoryItemViews(pwaApplication).stream())
        .collect(Collectors.toList());

    // get all person ids associated with history items
    var personIds = caseHistoryItemViews.stream()
        .map(CaseHistoryItemView::getPersonId)
        .map(PersonId::asInt)
        .collect(Collectors.toSet());

    // query people by ids and map by id
    Map<PersonId, Person> persons = personRepository.findAllByIdIn(personIds).stream()
        .collect(Collectors.toMap(Person::getId, Function.identity()));

    // set the person name on each item view, sort by datetime desc and return list
    return caseHistoryItemViews.stream()
        .peek(item -> item.setPersonName(getPersonName(persons, item.getPersonId())))
        .sorted(Comparator.comparing(CaseHistoryItemView::getDateTime, Collections.reverseOrder()))
        .collect(Collectors.toList());

  }

  private String getPersonName(Map<PersonId, Person> personIdToPersonMap, PersonId personId) {
    return personIdToPersonMap.get(personId).getFullName();
  }

}
