package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ConsentReviewRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsentReviewServiceTest {

  @Mock
  private ConsentReviewRepository consentReviewRepository;

  @Mock
  private Clock clock;

  private ConsentReviewService consentReviewService;

  @Captor
  private ArgumentCaptor<ConsentReview> consentReviewArgumentCaptor;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final Person person = PersonTestUtil.createDefaultPerson();

  private final Instant fixedInstant = Instant.now();

  @Before
  public void setUp() throws Exception {

    when(clock.instant()).thenReturn(fixedInstant);

    consentReviewService = new ConsentReviewService(consentReviewRepository, clock);

  }

  @Test
  public void startConsentReview_noOpenReviewExists() {

    consentReviewService.startConsentReview(detail, "my cover letter text", person);

    verify(consentReviewRepository, times(1)).save(consentReviewArgumentCaptor.capture());

    assertThat(consentReviewArgumentCaptor.getValue()).satisfies(review -> {
      assertThat(review.getPwaApplicationDetail()).isEqualTo(detail);
      assertThat(review.getCoverLetterText()).isEqualTo("my cover letter text");
      assertThat(review.getStatus()).isEqualTo(ConsentReviewStatus.OPEN);
      assertThat(review.getStartedByPersonId()).isEqualTo(person.getId());
      assertThat(review.getStartTimestamp()).isEqualTo(fixedInstant);
      assertThat(review.getEndedByPersonId()).isNull();
      assertThat(review.getEndTimestamp()).isNull();
      assertThat(review.getEndedReason()).isNull();
    });

  }

  @Test(expected = RuntimeException.class)
  public void startConsentReview_openReviewExists() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    consentReviewService.startConsentReview(detail, "error going to happen", person);

  }

  @Test
  public void getOpenConsentReview_openReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.OPEN);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var optionalReview = consentReviewService.getOpenConsentReview(detail);

    assertThat(optionalReview).isPresent();

  }

  @Test
  public void getOpenConsentReview_noOpenReview() {

    var openReview = new ConsentReview();
    openReview.setStatus(ConsentReviewStatus.APPROVED);
    when(consentReviewRepository.findAllByPwaApplicationDetail(detail)).thenReturn(List.of(openReview));

    var optionalReview = consentReviewService.getOpenConsentReview(detail);

    assertThat(optionalReview).isEmpty();

  }

}