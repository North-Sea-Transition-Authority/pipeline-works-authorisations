package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.options;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadConfirmationOfOptionTestUtil {

  private PadConfirmationOfOptionTestUtil() {
    // no-instantiation
  }

  public static PadConfirmationOfOption createConfirmationOfOption(PwaApplicationDetail pwaApplicationDetail){

    var confirmation = new PadConfirmationOfOption(pwaApplicationDetail);
    confirmation.setChosenOptionDesc("Some content");
    confirmation.setConfirmedOptionType(ConfirmedOptionType.WORK_COMPLETE_AS_PER_OPTIONS);

    ObjectTestUtils.assertAllFieldsNotNull(
        confirmation,
        PadConfirmationOfOption.class,
        Set.of(PadConfirmationOfOption_.ID)
    );
    return confirmation;
  }
}