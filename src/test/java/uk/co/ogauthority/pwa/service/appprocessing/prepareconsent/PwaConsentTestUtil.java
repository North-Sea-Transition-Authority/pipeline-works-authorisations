package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;

class PwaConsentTestUtil {



  static ConsentReview createApprovedConsentReview(PwaApplicationDetail pwaApplicationDetail) {
    var consentReview = new ConsentReview();
    consentReview.setStatus(ConsentReviewStatus.APPROVED);
    consentReview.setCoverLetterText("cover letter text");
    consentReview.setPwaApplicationDetail(pwaApplicationDetail);
    return consentReview;
  }


}
