package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;

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

    //get list with each item indexed according to its grouping order
    var caseHistoryItemViews = getIndexedCaseHitoryItemViews(pwaApplication);

    // get all person ids associated with history items
    var personIds = caseHistoryItemViews.stream()
        .map(CaseHistoryItemView::getPersonId)
        .collect(Collectors.toSet());

    // query people by ids and map by id
    Map<PersonId, Person> persons = personService.findAllByIdIn(personIds).stream()
        .collect(Collectors.toMap(Person::getId, Function.identity()));

    // set the person name and email if label provided on each item view, sort by datetime desc and return list
    caseHistoryItemViews.forEach(item -> {

      item.setPersonName(getPersonName(persons, item.getPersonId()));

      if (item.getPersonEmailLabel() != null) {
        item.setPersonEmail(getPersonEmail(persons, item.getPersonId()));
      }

    });

    caseHistoryItemViews.sort(Comparator.comparing(CaseHistoryItemView::getDateTime, Collections.reverseOrder()));

    return caseHistoryItemViews;

  }

  private List<CaseHistoryItemView> getIndexedCaseHitoryItemViews(PwaApplication pwaApplication) {
    return itemServices.stream().map(caseHistoryItemService -> {
      var views = caseHistoryItemService.getCaseHistoryItemViews(pwaApplication).stream()
          .sorted(Comparator.comparing(CaseHistoryItemView::getDateTime).reversed())
          .collect(toList());
      IntStream
          .range(0, views.size())
          .forEach(integer -> views.get(integer).setDisplayIndex(integer + 1));
      return views;
    }).flatMap(Collection::stream)
        .collect(toList());
  }

  private String getPersonName(Map<PersonId, Person> personIdToPersonMap, PersonId personId) {
    return personIdToPersonMap.get(personId).getFullName();
  }

  private String getPersonEmail(Map<PersonId, Person> personIdToPersonMap, PersonId personId) {
    return personIdToPersonMap.get(personId).getEmailAddress();
  }

}
