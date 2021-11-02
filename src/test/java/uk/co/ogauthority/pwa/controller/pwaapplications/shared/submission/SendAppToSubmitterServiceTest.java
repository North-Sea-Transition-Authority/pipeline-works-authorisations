package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.features.email.EmailCaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.ReviewAndSubmitApplicationEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class SendAppToSubmitterServiceTest {

  @Mock
  private NotifyService notifyService;

  @Spy
  private EmailCaseLinkService emailCaseLinkService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Captor
  private ArgumentCaptor<ReviewAndSubmitApplicationEmailProps> emailPropsCaptor;

  private SendAppToSubmitterService sendAppToSubmitterService;

  private PwaApplicationDetail detail;

  @Before
  public void setUp() throws Exception {

    sendAppToSubmitterService = new SendAppToSubmitterService(notifyService, emailCaseLinkService, applicationUpdateRequestService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  public void sendToSubmitter_updateTextPresent() {

    var sendingPerson = PersonTestUtil.createPersonFrom(new PersonId(1));
    var recipientPerson = PersonTestUtil.createPersonFrom(new PersonId(2));

    sendAppToSubmitterService.sendToSubmitter(detail, sendingPerson, "update text", recipientPerson);

    verify(applicationUpdateRequestService, times(1)).storeResponseWithoutSubmitting(detail, sendingPerson, "update text");

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(recipientPerson.getEmailAddress()));

    verifyEmailProps(sendingPerson, recipientPerson);

  }

  @Test
  public void sendToSubmitter_noUpdateText() {

    var sendingPerson = PersonTestUtil.createPersonFrom(new PersonId(1));
    var recipientPerson = PersonTestUtil.createPersonFrom(new PersonId(2));

    sendAppToSubmitterService.sendToSubmitter(detail, sendingPerson, null, recipientPerson);

    verify(applicationUpdateRequestService, times(0)).storeResponseWithoutSubmitting(any(), any(), any());

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(recipientPerson.getEmailAddress()));

    verifyEmailProps(sendingPerson, recipientPerson);

  }

  private void verifyEmailProps(Person sendingPerson, Person recipientPerson) {
    assertThat(emailPropsCaptor.getValue()).satisfies(props -> {
      assertThat(props.getApplicationReference()).isEqualTo(detail.getPwaApplicationRef());
      assertThat(props.getRequesterFullName()).isEqualTo(sendingPerson.getFullName());
      assertThat(props.getReviewAndSubmitPageUrl()).isEqualTo(emailCaseLinkService.generateReviewAndSubmitLink(detail.getPwaApplication()));
      assertThat(props.getRecipientFullName()).isEqualTo(recipientPerson.getFullName());
    });
  }

}