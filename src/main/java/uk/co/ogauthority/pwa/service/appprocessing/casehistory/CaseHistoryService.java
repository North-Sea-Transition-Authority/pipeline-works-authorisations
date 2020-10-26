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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.person.PersonService;

@Service
public class CaseHistoryService {

  private final List<? extends CaseHistoryItemService> itemServices;
  private final PersonService personService;

  @Autowired
  public CaseHistoryService(List<? extends CaseHistoryItemService> itemServices,
                            PersonService personService) {
    this.itemServices = itemServices;
    this.personService = personService;
  }

  public List<CaseHistoryItemView> getCaseHistory(PwaApplication pwaApplication) {

    var caseHistoryItemViews = itemServices.stream()
        .flatMap(itemService -> itemService.getCaseHistoryItemViews(pwaApplication).stream())
        .collect(Collectors.toList());

    // get all person ids associated with history items
    var personIds = caseHistoryItemViews.stream()
        .map(CaseHistoryItemView::getPersonId)
        .collect(Collectors.toSet());

    // query people by ids and map by id
    Map<PersonId, Person> persons = personService.findAllByIdIn(personIds).stream()
        .collect(Collectors.toMap(Person::getId, Function.identity()));

    // set the person name and email if label provided on each item view, sort by datetime desc and return list
    return caseHistoryItemViews.stream()
        .peek(item -> item.setPersonName(getPersonName(persons, item.getPersonId())))
        .peek(item -> {
          if (item.getPersonEmailLabel() != null) {
            item.setPersonEmail(getPersonEmail(persons, item.getPersonId()));
          }
        })
        .sorted(Comparator.comparing(CaseHistoryItemView::getDateTime, Collections.reverseOrder()))
        .collect(Collectors.toList());

  }

  private String getPersonName(Map<PersonId, Person> personIdToPersonMap, PersonId personId) {
    return personIdToPersonMap.get(personId).getFullName();
  }

  private String getPersonEmail(Map<PersonId, Person> personIdToPersonMap, PersonId personId) {
    return personIdToPersonMap.get(personId).getEmailAddress();
  }

}
