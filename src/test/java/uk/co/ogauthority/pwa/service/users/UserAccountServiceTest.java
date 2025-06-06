package uk.co.ogauthority.pwa.service.users;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
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
    var wua = userAccountService.getSystemWebUserAccount();
    verify(webUserAccountRepository, times(1)).findById(WUA_ID);
  }

  @Test
  void getPersonByEmailAddressOrLoginId_exactly1WuaFoundByEmailSearch_personReturned() {

    var person = PersonTestUtil.createDefaultPerson();
    var user = WebUserAccountTestUtil.createWebUserAccountMatchingPerson(1, person, WebUserAccountStatus.ACTIVE);

    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(
        person.getEmailAddress(), List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(List.of(user));

    var personOptional = userAccountService.getPersonByEmailAddressOrLoginId(person.getEmailAddress());

    assertThat(personOptional).isPresent();
    assertThat(personOptional.get().getEmailAddress()).isEqualTo(person.getEmailAddress());
  }

  @Test
  void getPersonByEmailAddressOrLoginId_exactly1WuaFoundByLoginIdSearch_personReturned() {

    var person = PersonTestUtil.createDefaultPerson();
    var user = WebUserAccountTestUtil.createWebUserAccount(1, person, "myLoginId", WebUserAccountStatus.ACTIVE);

    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(
        user.getLoginId(), List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(List.of(user));


    var personOptional = userAccountService.getPersonByEmailAddressOrLoginId(user.getLoginId());

    assertThat(personOptional).isPresent();
    assertThat(personOptional.get().getEmailAddress()).isEqualTo(person.getEmailAddress());
  }

  //An unlikely potential situation where two people have the same email address but with different user accounts that are active causing an error
  @Test
  void getPersonByEmailAddressOrLoginId_multiplePeopleFoundForSameEmail_exceptionThrown() {
    var emailAddress = "me@email.com";
    var person1 = PersonTestUtil.createPersonFrom(new PersonId(1), emailAddress);
    var user1 = WebUserAccountTestUtil.createWebUserAccount(1, person1, "myLoginId1", WebUserAccountStatus.ACTIVE);
    var person2 = PersonTestUtil.createPersonFrom(new PersonId(2), emailAddress);
    var user2 = WebUserAccountTestUtil.createWebUserAccount(1, person2, "myLoginId2", WebUserAccountStatus.ACTIVE);
    var user3 = WebUserAccountTestUtil.createWebUserAccount(2, person2, emailAddress, WebUserAccountStatus.ACTIVE);
    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(
        emailAddress, List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(new ArrayList<>(List.of(user1, user2)));
    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(
        user3.getLoginId(), List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(List.of(user3));

    assertThrows(RuntimeException.class, () ->
        userAccountService.getPersonByEmailAddressOrLoginId(person1.getEmailAddress()));
  }

  @Test
  void getPersonByEmailAddressOrLoginId_noPersonFoundForEmail_noPersonReturned() {

    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(any(), any())).thenReturn(new ArrayList<>());
    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(any(), any())).thenReturn(List.of());

    var personOptional = userAccountService.getPersonByEmailAddressOrLoginId("me@email.com");
    assertThat(personOptional).isEmpty();
  }

  @Test
  void getPersonByEmailAddressOrLoginId_multipleWuaForEmail_exactly1PersonForAccounts_personReturned() {

    var emailAddress = "me@email.com";
    var person1 = PersonTestUtil.createPersonFrom(new PersonId(1), emailAddress);
    var user1 = WebUserAccountTestUtil.createWebUserAccount(1, person1, "myLoginId1", WebUserAccountStatus.ACTIVE);
    var user2 = WebUserAccountTestUtil.createWebUserAccount(1, person1, "myLoginId2", WebUserAccountStatus.ACTIVE);

    when(webUserAccountRepository.findAllByEmailAddressIgnoreCaseAndAccountStatusNotIn(
        emailAddress, List.of(WebUserAccountStatus.CANCELLED, WebUserAccountStatus.NEW))).thenReturn(new ArrayList<>(List.of(user1, user2)));

    when(webUserAccountRepository.findAllByLoginIdIgnoreCaseAndAccountStatusNotIn(any(), any())).thenReturn(List.of());

    var personOptional = userAccountService.getPersonByEmailAddressOrLoginId(emailAddress);
    assertThat(personOptional).isPresent();
    assertThat(personOptional.get().getEmailAddress()).isEqualTo(emailAddress);
  }
}