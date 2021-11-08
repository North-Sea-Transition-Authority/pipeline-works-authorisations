package uk.co.ogauthority.pwa.service.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.feedback.Feedback;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.repository.feedback.FeedbackRepository;
import uk.co.ogauthority.pwa.validators.feedback.FeedbackValidator;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackServiceTest {

  @Mock
  private FeedbackValidator validationService;

  @Mock
  private FeedbackRepository feedbackRepository;

  private FeedbackService feedbackService;
  
  private Person person;

  @Before
  public void setup() {
    feedbackService = new FeedbackService(
        validationService,
        feedbackRepository
    );
    
    person = PersonTestUtil.createDefaultPerson();
  }

  @Test
  public void saveFeedback_whenNoApplicationDetailId_thenApplicationDetailIdIsNull() {

    var form = FeedbackTestUtil.getValidFeedbackForm();

    ArgumentCaptor<Feedback> feedbackArgumentCaptor = ArgumentCaptor.forClass(Feedback.class);

    feedbackService.saveFeedback(null, form, person);

    verify(feedbackRepository, times(1)).save(feedbackArgumentCaptor.capture());

    var persistedFeedback = feedbackArgumentCaptor.getValue();

    assertExpectedEntityProperties(form, persistedFeedback, person);
    assertThat(persistedFeedback.getPwaApplicationDetailId()).isNull();
  }

  @Test
  public void saveFeedback_whenApplicationDetailId_thenApplicationDetailIdIsNotNull() {

    var form = FeedbackTestUtil.getValidFeedbackForm();

    ArgumentCaptor<Feedback> feedbackArgumentCaptor = ArgumentCaptor.forClass(Feedback.class);

    var appDetailId = 10;
    feedbackService.saveFeedback(appDetailId, form, person);

    verify(feedbackRepository, times(1)).save(feedbackArgumentCaptor.capture());

    var persistedFeedback = feedbackArgumentCaptor.getValue();

    assertExpectedEntityProperties(form, persistedFeedback, person);
    assertThat(persistedFeedback.getPwaApplicationDetailId()).isEqualTo(appDetailId);
  }

  private void assertExpectedEntityProperties(FeedbackForm sourceForm, Feedback destinationEntity, Person submitter) {
    assertThat(destinationEntity.getRating()).isEqualTo(sourceForm.getServiceRating());
    assertThat(destinationEntity.getServiceFeedback()).isEqualTo(sourceForm.getFeedback());
    assertThat(destinationEntity.getSubmitterName()).isEqualTo(submitter.getFullName());
    assertThat(destinationEntity.getSubmitterEmailAddress()).isEqualTo(submitter.getEmailAddress());
    assertThat(destinationEntity.getSubmittedTimestamp()).isNotNull();
  }

}