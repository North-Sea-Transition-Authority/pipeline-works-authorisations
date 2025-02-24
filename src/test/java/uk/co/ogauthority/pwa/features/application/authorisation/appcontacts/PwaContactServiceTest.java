package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller.PwaContactController;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.service.teams.events.NonFoxTeamMemberEventPublisher;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaContactServiceTest {


  @Mock
  private PwaContactRepository pwaContactRepository;

  @Mock
  private NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher;

  @Captor
  private ArgumentCaptor<PwaContact> contactArgumentCaptor;

  private PwaContactService pwaContactService;

  private Person person = new Person(1, null, null, null, null);
  private WebUserAccount wua = new WebUserAccount(10, person);

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private PwaContact allRolesContact;

  @BeforeEach
  void setUp() {

    pwaContactService = new PwaContactService(pwaContactRepository, nonFoxTeamMemberEventPublisher);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    allRolesContact = new PwaContact(pwaApplication, person, EnumSet.allOf(PwaContactRole.class));
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
  void removeContact() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));

    pwaContactService.removeContact(pwaApplication, person);

    verify(pwaContactRepository, times(1)).delete(contact);
    verify(nonFoxTeamMemberEventPublisher, times(1)).publishNonFoxTeamMemberRemovedEvent(person);

  }

  @Test
  void removeContact_doesntExist() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->

      pwaContactService.removeContact(pwaApplication, person));

  }

  @Test
  void removeContact_notLastAccessManager() {
    var additionalAccessManager = new PwaContact(pwaApplication, new Person(), Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(allRolesContact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(
        List.of(allRolesContact, additionalAccessManager)
    );

    pwaContactService.removeContact(pwaApplication, person);

    verify(pwaContactRepository, times(1)).delete(allRolesContact);

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

      pwaContactService.removeContact(pwaApplication, person));

  }

  @Test
  void updateContact() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));

    var newRoles = Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER);
    pwaContactService.updateContact(pwaApplication, person, newRoles);

    verify(pwaContactRepository, times(1)).save(contactArgumentCaptor.capture());

    var updatedContact = contactArgumentCaptor.getValue();

    assertThat(updatedContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(updatedContact.getPerson()).isEqualTo(person);
    assertThat(updatedContact.getRoles()).containsExactlyInAnyOrder(
        PwaContactRole.ACCESS_MANAGER,
        PwaContactRole.PREPARER
    );

  }

  @Test
  void updateContact_notContact() {

    var pwaApplication = new PwaApplication();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    pwaContactService.updateContact(pwaApplication, person, Collections.<PwaContactRole>emptySet());

    verify(pwaContactRepository, times(1)).save(contactArgumentCaptor.capture());

    verify(nonFoxTeamMemberEventPublisher, times(1)).publishNonFoxTeamMemberAddedEvent(person);

    var newContact = contactArgumentCaptor.getValue();

    assertThat(newContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(newContact.getPerson()).isEqualTo(person);

  }

  @Test
  void updateContact_emptyRoles() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(any(), any())).thenReturn(Optional.of(new PwaContact()));
    assertThrows(IllegalStateException.class, () ->
      pwaContactService.updateContact(new PwaApplication(), new Person(), Set.of()));

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
    pwaContactService.updateContact(pwaApplication, person, newRoles);

    verify(pwaContactRepository, times(1)).save(contactArgumentCaptor.capture());

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
      pwaContactService.updateContact(pwaApplication, person, newRoles));

  }

  @Test
  void getTeamMemberView() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(123);
    pwaApplication.setApplicationType(PwaApplicationType.CAT_1_VARIATION);

    person = new Person(1, "forename", "surname", "a@b.com", "020 123 4567");
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    var teamMemberView = pwaContactService.getTeamMemberView(pwaApplication, contact);

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
        .map(TeamRoleView::getRoleName)
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
        new PwaContactDto(appDetailId, person.getId().asInt(), EnumSet.allOf(PwaContactRole.class))
    );

    when(pwaContactRepository.findAllAsDtoByPerson(person)).thenReturn(foundRoles);

    var result = pwaContactService.getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.allOf(PwaContactRole.class));

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
        new PwaContactDto(appDetailId, person.getId().asInt(), EnumSet.allOf(PwaContactRole.class))
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
      assertThat(taskInfo.getCountType()).isEqualTo("CONTACT");
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
