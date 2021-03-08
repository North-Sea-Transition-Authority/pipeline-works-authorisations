package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.time.Clock;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ConsentReviewRepository;

@Service
public class ConsentReviewService {

  private final ConsentReviewRepository consentReviewRepository;
  private final Clock clock;

  @Autowired
  public ConsentReviewService(ConsentReviewRepository consentReviewRepository,
                              @Qualifier("utcClock") Clock clock) {
    this.consentReviewRepository = consentReviewRepository;
    this.clock = clock;
  }

  @Transactional
  public void startConsentReview(PwaApplicationDetail pwaApplicationDetail,
                                 String coverLetterText,
                                 Person startingPerson) {

    // check no open review first
    boolean alreadyOpenReview = consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .anyMatch(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()));

    if (alreadyOpenReview) {
      throw new RuntimeException(String.format(
          "Can't start a new consent review as there is already an open one for PWA detail with id [%s]", pwaApplicationDetail.getId()));
    }

    var consentReview = new ConsentReview(pwaApplicationDetail, coverLetterText, startingPerson.getId(), clock.instant());
    consentReviewRepository.save(consentReview);

  }

  public Optional<ConsentReview> getOpenConsentReview(PwaApplicationDetail pwaApplicationDetail) {
    return consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .filter(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()))
        .findFirst();
  }

}
