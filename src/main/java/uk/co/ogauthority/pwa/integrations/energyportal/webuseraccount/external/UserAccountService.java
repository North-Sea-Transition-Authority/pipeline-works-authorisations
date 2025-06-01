package uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@Service
public class UserAccountService {

  private final int systemUserWuaId;

  private final WebUserAccountRepository webUserAccountRepository;

  @Autowired
  public UserAccountService(@Value("${pwa.global.system-user-wua-id}") int systemUserWuaId,
                            WebUserAccountRepository webUserAccountRepository) {
    this.systemUserWuaId = systemUserWuaId;
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public WebUserAccount getWebUserAccount(int wuaId) {
    return webUserAccountRepository.findById(wuaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Cannot find wua id: " + wuaId));
  }

  public Set<Person> getPersonsByWuaIdSet(Set<Integer> wuaIdSet) {
    return webUserAccountRepository.findAllByWuaIdIn(wuaIdSet).stream()
        .map(WebUserAccount::getLinkedPerson)
        .collect(Collectors.toSet());
  }

  public WebUserAccount getSystemWebUserAccount() {
    return getWebUserAccount(systemUserWuaId);
  }

  /**
   * Finds the Person linked to the WebUserAccount with the given email or loginId.
   */
  public Optional<Person> getPersonByEmailAddressOrLoginId(String emailOrLoginId) {

    var excludedWuaStatuses = List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW);

    List<WebUserAccount> webUserAccounts =
        webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(emailOrLoginId, excludedWuaStatuses);

    if (webUserAccounts.size() == 1) {
      return Optional.of(webUserAccounts.getFirst().getLinkedPerson());
    }

    webUserAccounts.addAll(
        webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(emailOrLoginId, excludedWuaStatuses));

    if (webUserAccounts.size() == 1) {
      return Optional.of(webUserAccounts.getFirst().getLinkedPerson());
    }

    Set<Person> distinctPeople = webUserAccounts.stream()
        .map(WebUserAccount::getLinkedPerson)
        .collect(Collectors.toSet());

    if (distinctPeople.size() > 1) {
      throw new RuntimeException(
          String.format("getPersonByEmailAddressOrLoginId returned %d different people with email/loginId '%s'",
              distinctPeople.size(), emailOrLoginId)
      );
    }

    if (distinctPeople.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(webUserAccounts.getFirst().getLinkedPerson());
  }
}
