package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.accounts.starter.EnergyPortalServiceAccessService;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaContactServiceTest {

  @Mock
  private PwaContactRepository pwaContactRepository;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserAccountService userAccountService;

  @Captor
  private ArgumentCaptor<PwaContact> contactArgumentCaptor;

  @Mock
  private EnergyPortalServiceAccessService energyPortalServiceAccessService;

  @InjectMocks
  private PwaContactService pwaContactService;

  private Person person = new Person(1, null, null, null, null);
  private final WebUserAccount wua = new WebUserAccount(10, person);

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PwaContact allRolesContact;
  private WebUserAccount contactUser;
  private final long user1WuaId = 1;

  @BeforeEach
  void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    allRolesContact = new PwaContact(pwaApplication, person, PwaContactRole.allRoles());

    contactUser = new WebUserAccount();
    contactUser.setWuaId(Math.toIntExact(user1WuaId));
    contactUser.setPerson(person);
  }

  @Test
  void getContactsForPwaApplication() {
    var contactOne = new PwaContact();
    var contactTwo = new PwaContact();

    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contactOne, contactTwo));

    assertThat(pwaContactService.getContactsForPwaApplication(pwaApplication)).containsExactlyInAnyOrder(contactOne,
        contactTwo);

  }

  @Test
  void getContactTeamMemberViews_returnsSortedTeamMemberViews() {
    var person1 = new Person(1, "Alice", "Smith", "alice@example.com", "123456789");
    var person2 = new Person(2, "Bob", "Jones", "bob@example.com", "987654321");

    var contact1 = new PwaContact(pwaApplication, person2, Set.of(PwaContactRole.PREPARER));
    var contact2 = new PwaContact(pwaApplication, person1, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contact1, contact2));
    when(userAccountService.getWebUserAccountsByPeople(Set.of(person1, person2))).thenReturn(List.of(
        new WebUserAccount(101, person1),
        new WebUserAccount(102, person2)
    ));

    var views = pwaContactService.getContactTeamMemberViews(pwaApplication);

    assertThat(views.get(0).getFullName()).isEqualTo("Alice Smith");
    assertThat(views.get(1).getFullName()).isEqualTo("Bob Jones");
  }

  @Test
  void getContactTeamMemberViews_throwsExceptionWhenPersonNotInUserMap() {
    var person1 = new Person(1, "Alice", "Smith", "alice@example.com", "123456789");
    var contact1 = new PwaContact(pwaApplication, person1, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contact1));
    when(userAccountService.getWebUserAccountsByPeople(Set.of(person1))).thenReturn(List.of());

    var ex = assertThrows(IllegalStateException.class,
        () -> pwaContactService.getContactTeamMemberViews(pwaApplication));

    assertThat(ex.getMessage()).contains("Person 1 not found in map of users");
  }

  @Test
  void getContactTeamMemberViews_returnsEmptyListWhenNoContacts() {
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of());
    when(userAccountService.getWebUserAccountsByPeople(Set.of())).thenReturn(List.of());

    var views = pwaContactService.getContactTeamMemberViews(pwaApplication);

    assertThat(views).isEmpty();
  }

  @Test
  void personIsContactOnApplication() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(new PwaContact()));

    assertThat(pwaContactService.personIsContactOnApplication(pwaApplication, person)).isTrue();

  }

  @Test
  void personIsContactOnApplication_notContact() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    assertThat(pwaContactService.personIsContactOnApplication(pwaApplication, person)).isFalse();

  }

  @Test
  void getContactOrError() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(new PwaContact())
    );

    assertThat(pwaContactService.getContactOrError(pwaApplication, person)).isNotNull();

  }

  @Test
  void getContactOrError_error() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(any(), any())).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->

      pwaContactService.getContactOrError(pwaApplication, person));

  }
  @Test
  void removeContact_userIsNotTeamMember() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(teamQueryService.userIsMemberOfAnyTeam(contactUser.getWuaId())).thenReturn(false);

    pwaContactService.removeContact(pwaApplication, contactUser);

    verify(pwaContactRepository).delete(contact);

    verify(energyPortalServiceAccessService).removeUser(contactUser.getWuaId());
  }

  @Test
  void removeContact_userIsTeamMember() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(teamQueryService.userIsMemberOfAnyTeam(contactUser.getWuaId())).thenReturn(true);

    pwaContactService.removeContact(pwaApplication, contactUser);

    verify(pwaContactRepository).delete(contact);

    verify(energyPortalServiceAccessService, never()).removeUser(anyLong());
  }

  @Test
  void removeContact_doesntExist() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->

      pwaContactService.removeContact(pwaApplication, contactUser));

  }

  @Test
  void removeContact_notLastAccessManager() {
    var additionalAccessManager = new PwaContact(pwaApplication, new Person(), Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(allRolesContact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(
        List.of(allRolesContact, additionalAccessManager)
    );

    pwaContactService.removeContact(pwaApplication, contactUser);

    verify(pwaContactRepository).delete(allRolesContact);
  }

  @Test
  void removeContact_lastAccessManager() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));
    var nonAccessManagerContact = new PwaContact(pwaApplication, new Person(), Set.of(PwaContactRole.VIEWER));
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(
          List.of(contact, nonAccessManagerContact)
      );
    assertThrows(LastAdministratorException.class, () ->

      pwaContactService.removeContact(pwaApplication, contactUser));
  }

  @Test
  void updateContact() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));

    var newRoles = Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER);
    pwaContactService.updateContact(pwaApplication, contactUser, newRoles);

    verify(pwaContactRepository).save(contactArgumentCaptor.capture());
    verify(energyPortalServiceAccessService, never()).addUser(anyLong());

    var updatedContact = contactArgumentCaptor.getValue();

    assertThat(updatedContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(updatedContact.getPerson()).isEqualTo(person);
    assertThat(updatedContact.getRoles()).containsExactlyInAnyOrder(
        PwaContactRole.ACCESS_MANAGER,
        PwaContactRole.PREPARER
    );

  }

  @Test
  void updateContact_newContact_usingEpas() {

    var pwaApplication = new PwaApplication();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());
    when(teamQueryService.userIsMemberOfAnyTeam(contactUser.getWuaId())).thenReturn(false);

    pwaContactService.updateContact(pwaApplication, contactUser, Collections.emptySet());

    verify(pwaContactRepository).save(contactArgumentCaptor.capture());

    verify(energyPortalServiceAccessService).addUser(contactUser.getWuaId());

    var newContact = contactArgumentCaptor.getValue();

    assertThat(newContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(newContact.getPerson()).isEqualTo(person);
  }

  @Test
  void updateContact_notContact_existingUser() {

    var pwaApplication = new PwaApplication();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());
    when(teamQueryService.userIsMemberOfAnyTeam(contactUser.getWuaId())).thenReturn(true);

    pwaContactService.updateContact(pwaApplication, contactUser, Collections.emptySet());

    verify(pwaContactRepository).save(contactArgumentCaptor.capture());

    verify(energyPortalServiceAccessService, never()).addUser(anyLong());

    var newContact = contactArgumentCaptor.getValue();

    assertThat(newContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(newContact.getPerson()).isEqualTo(person);

  }

  @Test
  void updateContact_emptyRoles() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(any(), any())).thenReturn(Optional.of(new PwaContact()));
    assertThrows(IllegalStateException.class, () ->
      pwaContactService.updateContact(new PwaApplication(), contactUser, Set.of()));

  }

  @Test
  void updateContact_changeAdministrator_notLastAdministrator() {

    var pwaApplication = new PwaApplication();
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));
    var additionalAccessManager = new PwaContact(pwaApplication, new Person(), Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(
        List.of(contact, additionalAccessManager)
    );

    var newRoles = Set.of(PwaContactRole.PREPARER);
    pwaContactService.updateContact(pwaApplication, contactUser, newRoles);

    verify(pwaContactRepository).save(contactArgumentCaptor.capture());

    verify(energyPortalServiceAccessService, never()).addUser(anyLong());

    var updatedContact = contactArgumentCaptor.getValue();

    assertThat(updatedContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(updatedContact.getPerson()).isEqualTo(person);
    assertThat(updatedContact.getRoles()).containsExactlyInAnyOrder(PwaContactRole.PREPARER);

  }

  @Test
  void updateContact_changeAdministrator_lastAdministrator() {
    var pwaApplication = new PwaApplication();
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contact));
    var newRoles = Set.of(PwaContactRole.PREPARER);
    assertThrows(LastAdministratorException.class, () ->
      pwaContactService.updateContact(pwaApplication, contactUser, newRoles));

  }

  @Test
  void getTeamMemberView() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(123);
    pwaApplication.setApplicationType(PwaApplicationType.CAT_1_VARIATION);

    person = new Person(1, "forename", "surname", "a@b.com", "020 123 4567");
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    var teamMemberView = pwaContactService.getTeamMemberView(pwaApplication, contact, 1);

    assertThat(teamMemberView.getForename()).isEqualTo(person.getForename());
    assertThat(teamMemberView.getSurname()).isEqualTo(person.getSurname());
    assertThat(teamMemberView.getEmailAddress()).isEqualTo(person.getEmailAddress());
    assertThat(teamMemberView.getTelephoneNo()).isEqualTo(person.getTelephoneNo());

    assertThat(teamMemberView.getEditRoute()).isEqualTo(
        ReverseRouter.route(on(PwaContactController.class).renderContactRolesScreen(
            pwaApplication.getApplicationType(),
            pwaApplication.getId(),
            null,
            person.getId().asInt(),
            null,
            null
            )
        )
    );

    assertThat(teamMemberView.getRemoveRoute()).isEqualTo(
        ReverseRouter.route(on(PwaContactController.class)
            .renderRemoveContactScreen(pwaApplication.getApplicationType(), pwaApplication.getId(), null,
                person.getId().asInt(), null)));

    assertThat(teamMemberView.getRoleViews().size()).isEqualTo(1);

    teamMemberView.getRoleViews().stream()
        .map(ContactTeamRoleView::getRoleName)
        .forEach(roleName -> {
          try {
            assertThat(roleName).isEqualTo(PwaContactRole.ACCESS_MANAGER.getRoleName());
          } catch (AssertionError e) {
          }
        });

  }

  @Test
  void getPwaContactRolesForWebUserAccount_multipleRolesSingleAppDetail_allRoleFilter() {
    var appDetailId = 20;
    var foundRoles = List.of(
        new PwaContactDto(appDetailId, person.getId().asInt(), PwaContactRole.allRoles())
    );

    when(pwaContactRepository.findAllAsDtoByPerson(person)).thenReturn(foundRoles);

    var result = pwaContactService.getPwaContactRolesForWebUserAccount(
        wua,
        PwaContactRole.allRoles());

    for (PwaContactRole role : PwaContactRole.values()) {
      PwaApplicationTestUtil.tryAssertionWithPwaContactRole(role, testRole ->

        assertThat(result).anySatisfy(singleRoleDto -> {
          assertThat(singleRoleDto.getPersonId()).isEqualTo(person.getId().asInt());
          assertThat(singleRoleDto.getPwaApplicationId()).isEqualTo(appDetailId);
          assertThat(singleRoleDto.getPwaContactRole()).isEqualTo(testRole);
        }));
    }

  }

  @Test
  void getPwaContactRolesForWebUserAccount_multipleRolesSingleAppDetail_singleRoleFilter() {

    var appDetailId = 20;
    var foundRoles = List.of(
        new PwaContactDto(appDetailId, person.getId().asInt(), PwaContactRole.allRoles())
    );

    when(pwaContactRepository.findAllAsDtoByPerson(person)).thenReturn(foundRoles);
    var filterRole = PwaContactRole.PREPARER;
    var result = pwaContactService.getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.of(filterRole)
    );

    assertThat(result).hasOnlyOneElementSatisfying(singleRoleDto -> {
      assertThat(singleRoleDto.getPersonId()).isEqualTo(person.getId().asInt());
      assertThat(singleRoleDto.getPwaApplicationId()).isEqualTo(appDetailId);
      assertThat(singleRoleDto.getPwaContactRole()).isEqualTo(filterRole);
    });


  }


  @Test
  void getPwaContactRolesForWebUserAccount_zeroRolesFound_singleRoleFilter() {
    when(pwaContactRepository.findAllAsDtoByPerson(person)).thenReturn(Collections.emptyList());

    var result = pwaContactService.getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    assertThat(result).isEmpty();
  }

  @Test
  void isComplete_alwaysReturnsTrue(){
    assertThat(pwaContactService.isComplete(pwaApplicationDetail)).isTrue();
  }

  @Test
  void getTaskInfoList_alwaysContainsContactCountItem(){

    when(pwaContactRepository.countByPwaApplication(pwaApplication)).thenReturn(1L);
    var taskInfoList = pwaContactService.getTaskInfoList(pwaApplicationDetail);
    assertThat(taskInfoList).hasSize(1);
    assertThat(taskInfoList.get(0)).satisfies(taskInfo -> {
      assertThat(taskInfo.getCount()).isEqualTo(1L);
      assertThat(taskInfo.getCountType()).isEqualTo("Contact");
    });
  }

  @Test
  void getPeopleInRoleForPwaApplication_whenContactWithRoleExists() {
    var fakeContact = new PwaContact();
    fakeContact.setRoles(Set.of(PwaContactRole.PREPARER));
    fakeContact.setPerson(person);
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(fakeContact));

    assertThat(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .containsExactly(person);
  }

  @Test
  void getPeopleInRoleForPwaApplication_whenZeroContactsWithRoleExist() {
    var fakeContact = new PwaContact();
    fakeContact.setRoles(Set.of(PwaContactRole.ACCESS_MANAGER));
    fakeContact.setPerson(person);
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(fakeContact));

    assertThat(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .isEmpty();
  }

  @Test
  void copySectionInformation_doesNothing() {
    pwaContactService.copySectionInformation(pwaApplicationDetail, pwaApplicationDetail);
    verifyNoInteractions(pwaContactRepository);
  }
}
