package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentEmailService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentDocumentEmailServiceTest {

  @Mock
  private NotifyService notifyService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private PwaTeamService pwaTeamService;

  private ConsentDocumentEmailService consentDocumentEmailService;

  @Captor
  private ArgumentCaptor<ConsentReviewEmailProps> emailPropsCaptor;

  @Captor
  private ArgumentCaptor<String> emailAddressCaptor;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final Person person = PersonTestUtil.createDefaultPerson();
  private final Person pwaManager1 = PersonTestUtil.createPersonFrom(new PersonId(10));
  private final Person pwaManager2 = PersonTestUtil.createPersonFrom(new PersonId(11));
  private final Set<Person> pwaManagers = Set.of(pwaManager1, pwaManager2);

  @Before
  public void setUp() throws Exception {

    when(caseLinkService.generateCaseManagementLink(any())).thenReturn("my case link");
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER)).thenReturn(pwaManagers);

    consentDocumentEmailService = new ConsentDocumentEmailService(notifyService, caseLinkService, pwaTeamService);

  }

  @Test
  public void sendConsentReviewStartedEmail() {

    consentDocumentEmailService.sendConsentReviewStartedEmail(detail, person);

    verify(notifyService, times(pwaManagers.size())).sendEmail(emailPropsCaptor.capture(), emailAddressCaptor.capture());

    assertThat(emailPropsCaptor.getAllValues()).allSatisfy(emailProps -> {

      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", detail.getPwaApplicationRef()),
          entry("SUBMITTING_USER_NAME", person.getFullName()),
          entry("CASE_MANAGEMENT_LINK", "my case link")
      );

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.CONSENT_REVIEW);

    });

    assertThat(emailPropsCaptor.getAllValues().get(0).getRecipientFullName()).isEqualTo(pwaManager1.getFullName());
    assertThat(emailAddressCaptor.getAllValues().get(0)).isEqualTo(pwaManager1.getEmailAddress());

    assertThat(emailPropsCaptor.getAllValues().get(1).getRecipientFullName()).isEqualTo(pwaManager2.getFullName());
    assertThat(emailAddressCaptor.getAllValues().get(1)).isEqualTo(pwaManager2.getEmailAddress());

  }

}