package uk.co.ogauthority.pwa.features.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.feedback.FeedbackValidator;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackServiceTest {

  private static final Instant DATETIME = Instant.parse("2020-04-29T10:15:30Z");
  private static final String SERVICE_NAME = "PWA";

  @Mock
  private FeedbackValidator validationService;

  @Mock
  private FeedbackClientService feedbackClientService;

  @Mock
  private PwaApplicationDetailRepository pwaApplicationDetailRepository;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private FeedbackEmailService feedbackEmailService;

  private FeedbackService feedbackService;
  
  private Person person;

  @Before
  public void setup() {

    Clock fixedClock = Clock.fixed(DATETIME, ZoneId.of("UTC"));

    feedbackService = new FeedbackService(
        validationService,
        feedbackClientService,
        pwaApplicationDetailRepository,
        caseLinkService,
        feedbackEmailService,
        SERVICE_NAME,
        fixedClock
    );
    
    person = PersonTestUtil.createDefaultPerson();
  }

  @Test
  public void saveFeedback_whenNoApplicationDetailId_thenTransactionIdIsNull() throws CannotSendFeedbackException {
    var form = FeedbackTestUtil.getValidFeedbackForm();

    ArgumentCaptor<Feedback> feedbackArgumentCaptor = ArgumentCaptor.forClass(Feedback.class);

    feedbackService.saveFeedback(form, person);

    verify(feedbackClientService, times(1)).saveFeedback(feedbackArgumentCaptor.capture());

    var persistedFeedback = feedbackArgumentCaptor.getValue();

    assertExpectedEntityProperties(form, persistedFeedback, person);
    assertThat(persistedFeedback.getTransactionId()).isNull();
  }

  @Test
  public void saveFeedback_whenApplicationDetailId_thenTransactionIdIsNotNull() throws CannotSendFeedbackException {
    var appId = 10;
    var appDetailId = 20;
    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.OPTIONS_VARIATION, 10, 20);

    when(pwaApplicationDetailRepository.findByPwaApplicationIdAndTipFlagIsTrue(appDetailId))
        .thenReturn(Optional.of(pwaApplicationDetail));

    var form = FeedbackTestUtil.getValidFeedbackForm();

    ArgumentCaptor<Feedback> feedbackArgumentCaptor = ArgumentCaptor.forClass(Feedback.class);

    feedbackService.saveFeedback(appDetailId, form, person);

    verify(feedbackClientService, times(1)).saveFeedback(feedbackArgumentCaptor.capture());

    var persistedFeedback = feedbackArgumentCaptor.getValue();

    assertExpectedEntityProperties(form, persistedFeedback, person);
    assertThat(persistedFeedback.getTransactionId()).isEqualTo(appId);
  }

  private void assertExpectedEntityProperties(FeedbackForm sourceForm, Feedback destinationEntity, Person submitter) {
    assertThat(destinationEntity.getSubmitterName()).isEqualTo(submitter.getFullName());
    assertThat(destinationEntity.getSubmitterEmail()).isEqualTo(submitter.getEmailAddress());
    assertThat(destinationEntity.getServiceRating()).isEqualTo(sourceForm.getServiceRating().name());
    assertThat(destinationEntity.getComment()).isEqualTo(sourceForm.getFeedback());
    assertThat(destinationEntity.getGivenDatetime()).isEqualTo(DATETIME);
  }

}