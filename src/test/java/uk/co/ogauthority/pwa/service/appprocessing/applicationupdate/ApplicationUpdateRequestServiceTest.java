package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateRequestServiceTest {
  private static final String TELEPHONE = "123456789";

  private static final int PREPARER_1_ID = 100;
  private static final int PREPARER_2_ID = 200;

  private static final String PREPARER_FORENAME = "PREP";
  private static final String PREPARER_1_SURNAME = "1";
  private static final String PREPARER_2_SURNAME = "2";
  private static final String PREPARER_1_FULL_NAME = "PREP 1";
  private static final String PREPARER_2_FULL_NAME = "PREP 2";

  private static final String PREPARER_1_EMAIL = "PREP_1@email.com";
  private static final String PREPARER_2_EMAIL = "PREP_2@email.com";

  private static final String REASON = "REASON";
  private static final int PERSON_ID = 1;

  @Mock
  private ApplicationUpdateRequestRepository applicationUpdateRequestRepository;

  @Mock
  private NotifyService notifyService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Captor
  private ArgumentCaptor<ApplicationUpdateRequest> appUpdateArgCapture;

  @Captor
  private ArgumentCaptor<String> stringArgCaptor;

  @Captor
  private ArgumentCaptor<EmailProperties> emailPropertiesArgumentCaptor;

  private Person person;
  private WebUserAccount user;

  private Person preparer1;
  private Person preparer2;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {
    person = new Person(PERSON_ID, "test", "person", "email", TELEPHONE);
    user = new WebUserAccount(99, person);
    preparer1 = new Person(PREPARER_1_ID, PREPARER_FORENAME, PREPARER_1_SURNAME, PREPARER_1_EMAIL, TELEPHONE);
    preparer2 = new Person(PREPARER_2_ID, PREPARER_FORENAME, PREPARER_2_SURNAME, PREPARER_2_EMAIL, TELEPHONE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    applicationUpdateRequestService = new ApplicationUpdateRequestService(
        applicationUpdateRequestRepository,
        clock,
        notifyService,
        pwaContactService,
        pwaApplicationDetailVersioningService
    );
  }

  @Test
  public void createApplicationUpdateRequest_savedRequestHasExpectedAttributes() {

    applicationUpdateRequestService.createApplicationUpdateRequest(pwaApplicationDetail, person, REASON);

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    var updateRequest = appUpdateArgCapture.getValue();

    assertThat(updateRequest.getRequestReason()).isEqualTo(REASON);
    assertThat(updateRequest.getRequestedByPersonId()).isEqualTo(new PersonId(PERSON_ID));
    assertThat(updateRequest.getRequestedTimestamp()).isEqualTo(clock.instant());
    assertThat(updateRequest.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void applicationDetailHasOpenUpdateRequest_serviceInteraction() {
    when(applicationUpdateRequestRepository.existsByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(true);
    assertThat(applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void sendApplicationUpdateRequestedEmail_whenMultiplePreparers() {

    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparer1, preparer2));

    applicationUpdateRequestService.sendApplicationUpdateRequestedEmail(pwaApplicationDetail, person);

    verify(notifyService, times(2)).sendEmail(emailPropertiesArgumentCaptor.capture(), stringArgCaptor.capture());

    var email1Properties = emailPropertiesArgumentCaptor.getAllValues().get(0);
    var email1Address = stringArgCaptor.getAllValues().get(0);

    var email2Properties = emailPropertiesArgumentCaptor.getAllValues().get(1);
    var email2Address = stringArgCaptor.getAllValues().get(1);

    assertThat(emailPropertiesArgumentCaptor.getAllValues()).allSatisfy(emailProperties ->
        assertThat(emailProperties.getTemplate().equals(NotifyTemplate.APPLICATION_UPDATE_REQUESTED))
    );

    assertThat(email1Address).isEqualTo(PREPARER_1_EMAIL);
    assertEmailPropertiesAsExpected(email1Properties, PREPARER_1_FULL_NAME);

    assertThat(email2Address).isEqualTo(PREPARER_2_EMAIL);
    assertEmailPropertiesAsExpected(email2Properties, PREPARER_2_FULL_NAME);
  }

  @Test
  public void sendApplicationUpdateRequestedEmail_wheZeroPreparers() {

    applicationUpdateRequestService.sendApplicationUpdateRequestedEmail(pwaApplicationDetail, person);

    verify(notifyService, times(0)).sendEmail(any(), any());
  }


  @Test
  public void submitApplicationUpdateRequest_serviceInteractions(){
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparer1));

    // dont bother creating new tip app detail. just use existing one.
    when(pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, user))
        .thenReturn(pwaApplicationDetail);

    applicationUpdateRequestService.submitApplicationUpdateRequest(pwaApplicationDetail, user, REASON);
    verify(applicationUpdateRequestRepository, times(1)).save(any());
    verify(notifyService, times(1)).sendEmail(any(), eq(PREPARER_1_EMAIL));
    verify(pwaApplicationDetailVersioningService, times(1)).createNewApplicationVersion(pwaApplicationDetail, user);

  }

  private void assertEmailPropertiesAsExpected(EmailProperties emailProperties,
                                               String recipientFullName) {

    assertThat(emailProperties.getEmailPersonalisation()).contains(
        entry("CASE_OFFICER_NAME", person.getFullName()),
        entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef())
    );
    assertThat(emailProperties.getRecipientFullName()).isEqualTo(recipientFullName);

  }
}