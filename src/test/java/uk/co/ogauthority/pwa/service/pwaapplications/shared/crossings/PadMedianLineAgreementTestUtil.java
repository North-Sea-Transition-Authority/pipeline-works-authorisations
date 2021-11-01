package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadMedianLineAgreementTestUtil {

  private PadMedianLineAgreementTestUtil() {
    // no instantiaion
  }

  public static PadMedianLineAgreement createPadMedianLineAgreement(PwaApplicationDetail pwaApplicationDetail) {

    var medianLineAgreement = new PadMedianLineAgreement();
    var randomString = RandomStringUtils.randomAlphabetic(10);
    medianLineAgreement.setPwaApplicationDetail(pwaApplicationDetail);
    medianLineAgreement.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    medianLineAgreement.setNegotiatorEmail(randomString + "@email.com");
    medianLineAgreement.setNegotiatorName(randomString);

    ObjectTestUtils.assertAllFieldsNotNull(
        medianLineAgreement,
        PadMedianLineAgreement.class,
        Set.of(PadMedianLineAgreement_.ID));

    return medianLineAgreement;

  }
}
