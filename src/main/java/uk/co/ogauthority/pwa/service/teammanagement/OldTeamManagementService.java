package uk.co.ogauthority.pwa.service.teammanagement;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.internal.PersonRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@Service
public class OldTeamManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OldTeamManagementService.class);

  private final PersonRepository personRepository;
  private final WebUserAccountRepository webUserAccountRepository;

  public OldTeamManagementService(PersonRepository personRepository,
                                  WebUserAccountRepository webUserAccountRepository
  ) {
    this.personRepository = personRepository;
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public Person getPerson(int personId) {
    return personRepository.findById(personId).orElseThrow(
        () -> new PwaEntityNotFoundException("Person with id " + personId + " not found"));
  }

  /**
   * Finds the Person linked to the WebUserAccount with the given email or loginId.
   */
  public Optional<Person> getPersonByEmailAddressOrLoginId(String emailOrLoginId) {

    var excludedWuaStatuses = List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW);

    List<WebUserAccount> webUserAccounts =
        webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(emailOrLoginId, excludedWuaStatuses);

    if (webUserAccounts.size() == 1) {
      return Optional.of(webUserAccounts.get(0).getLinkedPerson());
    }

    webUserAccounts.addAll(
        webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(emailOrLoginId, excludedWuaStatuses));

    if (webUserAccounts.size() == 1) {
      return Optional.of(webUserAccounts.get(0).getLinkedPerson());
    } else {

      Set<Person> distinctPeople = webUserAccounts.stream()
          .map(WebUserAccount::getLinkedPerson)
          .collect(Collectors.toSet());

      if (distinctPeople.size() > 1) {
        throw new RuntimeException(
            String.format("getPersonByEmailAddressOrLoginId returned %d different people with email/loginId '%s'",
                distinctPeople.size(), emailOrLoginId)
        );
      } else if (distinctPeople.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(webUserAccounts.get(0).getLinkedPerson());
      }

    }
  }
}