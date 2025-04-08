package uk.co.ogauthority.pwa.integrations.govuknotify;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.NotificationLibraryClient;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.TemplateType;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.features.email.teammangement.AddedToTeamEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.epa.correlationid.CorrelationIdUtil;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  private static final NotifyTemplate NOTIFY_TEMPLATE = NotifyTemplate.ADDED_MEMBER_TO_TEAM;
  private static final Template TEMPLATE = new Template(
      NOTIFY_TEMPLATE.getTemplateId(),
      TemplateType.EMAIL,
      Set.of(),
      Template.VerificationStatus.CONFIRMED_NOTIFY_TEMPLATE
  );

  @Mock
  private NotificationLibraryClient notificationLibraryClient;

  @Mock
  private ServiceProperties serviceProperties;

  @InjectMocks
  private EmailService emailService;

  @Test
  void sendEmail() {
    var person = new Person();
    person.setForename("firstname");
    person.setSurname("lastname");
    person.setEmailAddress("a@b.com");

    var emailProperties = new AddedToTeamEmailProps("firstname lastname", "teamname", "");

    var domainId = "domainId";

    var correlationId = "correlation-id";
    CorrelationIdUtil.setCorrelationIdOnMdc(correlationId);

    when(notificationLibraryClient.isRunningTestMode())
        .thenReturn(true);
    when(notificationLibraryClient.getTemplate(NOTIFY_TEMPLATE.getTemplateId()))
        .thenReturn(TEMPLATE);

    emailService.sendEmail(emailProperties, person, domainId);

    verify(notificationLibraryClient).sendEmail(any(MergedTemplate.class), eq(person), any(DomainReference.class), eq(correlationId));
  }
}