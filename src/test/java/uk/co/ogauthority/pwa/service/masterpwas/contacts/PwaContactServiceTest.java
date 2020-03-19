package uk.co.ogauthority.pwa.service.masterpwas.contacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.contacts.PwaContact;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.masterpwas.contacts.PwaContactRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

@RunWith(MockitoJUnitRunner.class)
public class PwaContactServiceTest {

  @Mock
  private PwaContactRepository pwaContactRepository;

  @Captor
  private ArgumentCaptor<PwaContact> contactArgumentCaptor;

  private PwaContactService pwaContactService;

  @Before
  public void setUp() {
    pwaContactService = new PwaContactService(pwaContactRepository);
  }

  @Test
  public void addContact() {

    var pwaApplication = new PwaApplication();
    var person = new Person(1, "fore", "sur", "a@b.com", "012358594389");
    pwaContactService.addContact(pwaApplication, person, Set.of(PwaContactRole.PREPARER));

    verify(pwaContactRepository, times(1)).save(contactArgumentCaptor.capture());

    PwaContact newContact = contactArgumentCaptor.getValue();

    assertThat(newContact.getPwaApplication()).isEqualTo(pwaApplication);
    assertThat(newContact.getPerson()).isEqualTo(person);
    assertThat(newContact.getRoles()).containsExactly(PwaContactRole.PREPARER);

  }

  @Test
  public void getContactsForPwaApplication() {

    var pwaApplication = new PwaApplication();

    var contactOne = new PwaContact();
    var contactTwo = new PwaContact();

    when(pwaContactRepository.findAllByPwaApplication(pwaApplication)).thenReturn(List.of(contactOne, contactTwo));

    assertThat(pwaContactService.getContactsForPwaApplication(pwaApplication)).containsExactlyInAnyOrder(contactOne, contactTwo);

  }

  @Test
  public void personIsContactOnApplication() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(new PwaContact()));

    assertThat(pwaContactService.personIsContactOnApplication(pwaApplication, person)).isTrue();

  }

  @Test
  public void personIsContactOnApplication_notContact() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    assertThat(pwaContactService.personIsContactOnApplication(pwaApplication, person)).isFalse();

  }

  @Test
  public void personHasContactRoleForPwaApplication_personHasRole() {

    var pwaApplication = new PwaApplication();
    var person = new Person();
    var roles = Set.of(PwaContactRole.ACCESS_MANAGER, PwaContactRole.PREPARER);

    var pwaContact = new PwaContact(pwaApplication, person, roles);

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.of(pwaContact));

    assertThat(pwaContactService.personHasContactRoleForPwaApplication(pwaApplication, person, PwaContactRole.ACCESS_MANAGER)).isTrue();
    assertThat(pwaContactService.personHasContactRoleForPwaApplication(pwaApplication, person, PwaContactRole.PREPARER)).isTrue();
    assertThat(pwaContactService.personHasContactRoleForPwaApplication(pwaApplication, person, PwaContactRole.VIEWER)).isFalse();

  }

  @Test
  public void personHasContactRoleForPwaApplication_notContact() {

    var pwaApplication = new PwaApplication();
    var person = new Person();

    when(pwaContactRepository.findByPwaApplicationAndPerson(pwaApplication, person)).thenReturn(Optional.empty());

    Arrays.stream(PwaContactRole.values()).forEach(role ->
        assertThat(pwaContactService.personHasContactRoleForPwaApplication(pwaApplication, person, role)).isFalse());

  }

}
