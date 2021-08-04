package uk.co.ogauthority.pwa.service.testharness;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.WebUserAccountStatus;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.repository.WebUserAccountRepository;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
@Profile("test-harness")
public class TestHarnessUserRetrievalService {

  private final PersonService personService;
  private final TeamService teamService;
  private final WebUserAccountRepository webUserAccountRepository;


  @Autowired
  public TestHarnessUserRetrievalService(
      PersonService personService,
      TeamService teamService, WebUserAccountRepository webUserAccountRepository) {
    this.personService = personService;
    this.teamService = teamService;
    this.webUserAccountRepository = webUserAccountRepository;
  }



  public WebUserAccount getWebUserAccount(Integer personId) {

    var person = personService.getPersonById(new PersonId(personId));

    return webUserAccountRepository.findAllByPersonAndAccountStatusIn(
        person, List.of(WebUserAccountStatus.ACTIVE, WebUserAccountStatus.NEW))
        .stream().findAny().orElseThrow(() -> new PwaEntityNotFoundException(
            "Could not find web user account for person id: " + personId));
  }


  public AuthenticatedUserAccount createAuthenticatedUserAccount(Integer personId) {

    var webUserAccount = getWebUserAccount(personId);
    return createAuthenticatedUserAccount(webUserAccount);
  }

  public AuthenticatedUserAccount createAuthenticatedUserAccount(WebUserAccount webUserAccount) {
    var userPrivs = teamService.getAllUserPrivilegesForPerson(webUserAccount.getLinkedPerson());
    return new AuthenticatedUserAccount(webUserAccount, userPrivs);
  }






}
