package uk.co.ogauthority.pwa.service.masterpwas.contacts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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

}
