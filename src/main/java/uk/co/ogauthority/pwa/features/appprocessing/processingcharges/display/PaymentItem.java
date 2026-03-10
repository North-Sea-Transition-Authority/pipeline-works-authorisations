package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display;

import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;

public interface PaymentItem {

  PwaApplicationFeeType getPwaApplicationFeeType();

  String getDescription();

  int getPennyAmount();
}
