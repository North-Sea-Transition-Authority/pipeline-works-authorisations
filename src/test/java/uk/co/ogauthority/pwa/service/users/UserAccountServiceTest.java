package uk.co.ogauthority.pwa.service.users;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

  private static final int WUA_ID = 1;

  @Mock
  private WebUserAccountRepository webUserAccountRepository;

  private UserAccountService userAccountService;

  @BeforeEach
  void setup(){
    userAccountService = new UserAccountService(WUA_ID, webUserAccountRepository);
  }

  @Test
  void getSystemWebUserAccount_serviceInteractions() {
    when(webUserAccountRepository.findById(any())).thenReturn(Optional.of(new WebUserAccount()));
    userAccountService.getSystemWebUserAccount();
    verify(webUserAccountRepository, times(1)).findById(WUA_ID);
  }

  @Test
  void findByPerson_whenExists_thenReturn() {

    Person person = PersonTestUtil.createDefaultPerson();

    WebUserAccount webUserAccount = WebUserAccountTestUtil.createWebUserAccount(
        WUA_ID,
        person,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    when(webUserAccountRepository.findByPerson(person))
        .thenReturn(List.of(webUserAccount));

    var resultingWebUserAccount = userAccountService.findByPerson(person);

    assertThat(resultingWebUserAccount).isEqualTo(Optional.of(webUserAccount));
  }

  @Test
  void findByPerson_whenDoesNotExists_thenEmpty() {

    Person person = PersonTestUtil.createDefaultPerson();

    when(webUserAccountRepository.findByPerson(person))
        .thenReturn(List.of());

    var resultingWebUserAccount = userAccountService.findByPerson(person);

    assertThat(resultingWebUserAccount).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = WebUserAccountStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "ACTIVE")
  void findByPerson_whenMultipleAccounts_activeOnly(WebUserAccountStatus nonActiveWebUserAccountStatus) {

    Person person = PersonTestUtil.createDefaultPerson();

    WebUserAccount activeWebUserAccount = WebUserAccountTestUtil.createWebUserAccount(
        10,
        person,
        "loginId",
        WebUserAccountStatus.ACTIVE
    );

    WebUserAccount cancelledWebUserAccount = WebUserAccountTestUtil.createWebUserAccount(
        20,
        person,
        "loginId",
        nonActiveWebUserAccountStatus
    );

    when(webUserAccountRepository.findByPerson(person))
        .thenReturn(List.of(activeWebUserAccount, cancelledWebUserAccount));

    var resultingWebUserAccount = userAccountService.findByPerson(person);

    assertThat(resultingWebUserAccount).isEqualTo(Optional.of(activeWebUserAccount));

  }

  @Test
  void findByPerson_whenNoActiveAccounts_activeOnly() {

    Person person = PersonTestUtil.createDefaultPerson();

    WebUserAccount cancelledWebUserAccount = WebUserAccountTestUtil.createWebUserAccount(
        10,
        person,
        "loginId",
        WebUserAccountStatus.CANCELLED
    );

    WebUserAccount suspendedWebUserAccount = WebUserAccountTestUtil.createWebUserAccount(
        20,
        person,
        "loginId",
        WebUserAccountStatus.SUSPENDED
    );

    when(webUserAccountRepository.findByPerson(person))
        .thenReturn(List.of(suspendedWebUserAccount, cancelledWebUserAccount));

    var resultingWebUserAccount = userAccountService.findByPerson(person);

    assertThat(resultingWebUserAccount).isEmpty();

  }
}