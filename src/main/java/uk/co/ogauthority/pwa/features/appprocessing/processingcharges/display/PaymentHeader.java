package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display;

import java.util.List;

public interface PaymentHeader<T extends PaymentItem>  {

  int getTotalPennies();

  String getSummary();

  List<T> getPaymentItems();
}
