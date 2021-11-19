package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PwaConsentReviewTestUtil {

  private PwaConsentReviewTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }


  public static ConsentReview createApprovedConsentReview(PwaApplicationDetail pwaApplicationDetail) {
    var consentReview = new ConsentReview();
    consentReview.setStatus(ConsentReviewStatus.APPROVED);
    consentReview.setCoverLetterText("cover letter text");
    consentReview.setPwaApplicationDetail(pwaApplicationDetail);
    return consentReview;
  }


}
