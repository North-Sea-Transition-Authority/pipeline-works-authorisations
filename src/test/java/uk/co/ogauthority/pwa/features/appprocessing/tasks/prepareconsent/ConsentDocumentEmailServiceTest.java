package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentDocumentEmailService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ConsentReviewEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentDocumentEmailServiceTest {

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private ConsentDocumentEmailService consentDocumentEmailService;

  @Captor
  private ArgumentCaptor<ConsentReviewEmailProps> emailPropsCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientArgumentCaptor;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final Person person = PersonTestUtil.createDefaultPerson();
  private final TeamMemberView pwaManager1 = new TeamMemberView(1L, "Mr.", "PWA", "Manager1", "manager1@pwa.co.uk", null,
      null, null);
  private final TeamMemberView pwaManager2 = new TeamMemberView(2L, "Ms.", "PWA", "Manager2", "manager2@pwa.co.uk", null,
      null, null);
  private final Set<TeamMemberView> pwaManagers = Set.of(pwaManager1, pwaManager2);

  @BeforeEach
  void setUp() {

    when(caseLinkService.generateCaseManagementLink(any())).thenReturn("my case link");
    when(teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER)).thenReturn(List.of(pwaManager1, pwaManager2));

  }

  @Test
  void sendConsentReviewStartedEmail() {

    consentDocumentEmailService.sendConsentReviewStartedEmail(detail, person);

    verify(emailService, times(pwaManagers.size())).sendEmail(emailPropsCaptor.capture(), emailRecipientArgumentCaptor.capture(), eq(detail.getPwaApplicationRef()));

    assertThat(emailPropsCaptor.getAllValues()).allSatisfy(emailProps -> {

      assertThat(emailProps.getEmailPersonalisation()).contains(
          entry("APPLICATION_REFERENCE", detail.getPwaApplicationRef()),
          entry("SUBMITTING_USER_NAME", person.getFullName()),
          entry("CASE_MANAGEMENT_LINK", "my case link")
      );

      assertThat(emailProps.getTemplate()).isEqualTo(NotifyTemplate.CONSENT_REVIEW);

    });

    assertThat(emailPropsCaptor.getAllValues().get(0).getRecipientFullName()).isEqualTo(pwaManager1.getFullName());
    assertThat(emailRecipientArgumentCaptor.getAllValues().get(0).getEmailAddress()).isEqualTo(pwaManager1.email());

    assertThat(emailPropsCaptor.getAllValues().get(1).getRecipientFullName()).isEqualTo(pwaManager2.getFullName());
    assertThat(emailRecipientArgumentCaptor.getAllValues().get(1).getEmailAddress()).isEqualTo(pwaManager2.email());

  }

}