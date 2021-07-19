package uk.co.ogauthority.pwa.service.testharness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.repository.WebUserAccountRepository;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.person.PersonService;

@Service
public class TestHarnessUserRetrievalService {

  private final PersonService personService;
  private final WebUserAccountRepository webUserAccountRepository;




  @Autowired
  public TestHarnessUserRetrievalService(
      PersonService personService,
      WebUserAccountRepository webUserAccountRepository) {
    this.personService = personService;
    this.webUserAccountRepository = webUserAccountRepository;
  }



  WebUserAccount getWebUserAccount(Integer applicantPersonId) {

    var person = personService.getPersonById(new PersonId(applicantPersonId));

    return webUserAccountRepository.findAllByPersonAndAccountStatus(person, WebUserAccountStatus.ACTIVE)
    .stream().findAny().orElseThrow(() -> new PwaEntityNotFoundException(
        "Could not find web user account for person id: " + applicantPersonId));
  }






}
