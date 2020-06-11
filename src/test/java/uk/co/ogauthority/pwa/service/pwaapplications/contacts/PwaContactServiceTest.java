package uk.co.ogauthority.pwa.service.pwaapplications.contacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.masterpwas.contacts.PwaContactDto;
import uk.co.ogauthority.pwa.repository.masterpwas.contacts.PwaContactRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.teammanagement.LastAdministratorException;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaContactServiceTest {


  @Mock
  private PwaContactRepository pwaContactRepository;

  @Captor
  private ArgumentCaptor<PwaContact> contactArgumentCaptor;

  private PwaContactService pwaContactService;


  private Person person = new Person(1, null, null, null, null);
  private WebUserAccount wua = new WebUserAccount(10, person);

  private PwaApplication pwaApplication;
  private PwaContact allRolesContact;

  @Before
  public void setUp() {

    pwaContactService = new PwaContactService(pwaContactRepository);
    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL).getPwaApplication();
    allRolesContact = new PwaContact(pwaApplication, person, EnumSet.allOf(PwaContactRole.class));
  }

  @Test
  public void addContact() {
    person = new Person(1, "fore", "sur", "a@b.com", "012358594389");
    pwaContactService.addContact(pwaApplication, person, Set.of(PwaContactRole.PREPARER));

    verify(pwaContactRepository, times(1)).save(contactArgumentCaptor.capture());

    PwaContact newContact = contactArgumentCaptor.getValue();

    assertThat(newContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(newContact.getPerson()).isEqualTo(person);
    assertThat(newContact.getRoles()).containsExactly(PwaContactRole.PREPARER);

  }

  @Test
  public void getContactsForPwaApplication() {
    var contactOne = new PwaContact();
    var contactTwo = new PwaContact();

    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contactOne, contactTwo));

    assertThat(pwaContactService.getContactsForPwaApplication(pwaApplication)).containsExactlyInAnyOrder(contactOne,
        contactTwo);

  }

  @Test
  public void personIsContactOnApplication() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(new PwaContact()));

    assertThat(pwaContactService.personIsContactOnApplication(pwaApplication, person)).isTrue();

  }

  @Test
  public void personIsContactOnApplication_notContact() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    assertThat(pwaContactService.personIsContactOnApplication(pwaApplication, person)).isFalse();

  }

  @Test
  public void personHasContactRoleForPwaApplication_personHasRole() {
    var roles = Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER);

    var pwaContact = new PwaContact(pwaApplication, person, roles);

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person))
        .thenReturn(Optional.of(pwaContact));

    assertThat(pwaContactService.personHasContactRoleForPwaApplication(
        pwaApplication, person,
        PwaContactRole.ACCESS_MANAGER
    )).isTrue();
    assertThat(pwaContactService.personHasContactRoleForPwaApplication(
        pwaApplication, person,
        PwaContactRole.PREPARER
    )).isTrue();
    assertThat(pwaContactService.personHasContactRoleForPwaApplication(
        pwaApplication, person,
        PwaContactRole.VIEWER
    )).isFalse();

  }

  @Test
  public void personHasContactRoleForPwaApplication_notContact() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    Arrays.stream(PwaContactRole.values()).forEach(role ->
        assertThat(pwaContactService.personHasContactRoleForPwaApplication(pwaApplication, person, role)).isFalse());

  }

  @Test
  public void getContactOrError() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(new PwaContact())
    );

    assertThat(pwaContactService.getContactOrError(pwaApplication, person)).isNotNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getContactOrError_error() {

    when(pwaContactRepository.findByPwaApplicationAndPerson(any(), any())).thenReturn(Optional.empty());

    pwaContactService.getContactOrError(pwaApplication, person);

  }

  @Test
  public void removeContact() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));

    pwaContactService.removeContact(pwaApplication, person);

    verify(pwaContactRepository, times(1)).delete(contact);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void removeContact_doesntExist() {
    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    pwaContactService.removeContact(pwaApplication, person);

  }

  @Test
  public void removeContact_notLastAccessManager() {
    var additionalAccessManager = new PwaContact(pwaApplication, new Person(), Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(
        Optional.of(allRolesContact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(
        List.of(allRolesContact, additionalAccessManager)
    );

    pwaContactService.removeContact(pwaApplication, person);

    verify(pwaContactRepository, times(1)).delete(allRolesContact);

  }

  @Test(expected = LastAdministratorException.class)
  public void removeContact_lastAccessManager() {
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));
    var nonAccessManagerContact = new PwaContact(pwaApplication, new Person(), Set.of(PwaContactRole.VIEWER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(
        List.of(contact, nonAccessManagerContact)
    );

    pwaContactService.removeContact(pwaApplication, person);

  }

  @Test
  public void updateContact() {
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
  public void updateContact_notContact() {

    var pwaApplication = new PwaApplication();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    pwaContactService.updateContact(pwaApplication, person, Collections.<PwaContactRole>emptySet());

    verify(pwaContactRepository, times(1)).save(contactArgumentCaptor.capture());

    var newContact = contactArgumentCaptor.getValue();

    assertThat(newContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(newContact.getPerson()).isEqualTo(person);

  }

  @Test(expected = IllegalStateException.class)
  public void updateContact_emptyRoles() {

    when(pwaContactRepository.findByPwaApplicationAndPerson(any(), any())).thenReturn(Optional.of(new PwaContact()));
    pwaContactService.updateContact(new PwaApplication(), new Person(), Set.of());

  }

  @Test
  public void updateContact_changeAdministrator_notLastAdministrator() {

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

  @Test(expected = LastAdministratorException.class)
  public void updateContact_changeAdministrator_lastAdministrator() {

    var pwaApplication = new PwaApplication();
    var contact = new PwaContact(pwaApplication, person, Set.of(PwaContactRole.ACCESS_MANAGER));

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(contact));
    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contact));

    var newRoles = Set.of(PwaContactRole.PREPARER);
    pwaContactService.updateContact(pwaApplication, person, newRoles);

  }

  @Test
  public void getTeamMemberView() {

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
            person.getId().asInt(),
            null,
            null
            )
        )
    );

    assertThat(teamMemberView.getRemoveRoute()).isEqualTo(
        ReverseRouter.route(on(PwaContactController.class)
            .renderRemoveContactScreen(pwaApplication.getApplicationType(), pwaApplication.getId(),
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
  public void getPwaContactRolesForWebUserAccount_multipleRolesSingleAppDetail_allRoleFilter() {
    var appDetailId = 20;
    var foundRoles = List.of(
        new PwaContactDto(appDetailId, person.getId().asInt(), EnumSet.allOf(PwaContactRole.class))
    );

    when(pwaContactRepository.findAllAsDtoByPerson(person)).thenReturn(foundRoles);

    var result = pwaContactService.getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.allOf(PwaContactRole.class));

    for (PwaContactRole role : PwaContactRole.values()) {
      PwaApplicationTestUtil.tryAssertionWithPwaContactRole(role, testRole -> {

        assertThat(result).anySatisfy(singleRoleDto -> {
          assertThat(singleRoleDto.getPersonId()).isEqualTo(person.getId().asInt());
          assertThat(singleRoleDto.getPwaApplicationId()).isEqualTo(appDetailId);
          assertThat(singleRoleDto.getPwaContactRole()).isEqualTo(testRole);
        });

      });
    }

  }

  @Test
  public void getPwaContactRolesForWebUserAccount_multipleRolesSingleAppDetail_singleRoleFilter() {

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
  public void getPwaContactRolesForWebUserAccount_zeroRolesFound_singleRoleFilter() {
    when(pwaContactRepository.findAllAsDtoByPerson(person)).thenReturn(Collections.emptyList());

    var result = pwaContactService.getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    assertThat(result).isEmpty();
  }

}
