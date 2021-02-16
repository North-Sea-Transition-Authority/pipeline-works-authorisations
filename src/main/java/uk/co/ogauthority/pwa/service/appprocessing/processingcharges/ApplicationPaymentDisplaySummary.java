package uk.co.ogauthority.pwa.service.appprocessing.processingcharges;

import java.util.List;

/* Application fee or charge summary object designed for the consumtion by frontend templates.
 */
public class ApplicationPaymentDisplaySummary {

  private final String headlineSummary;
  private final String formattedAmount;

  private final List<DisplayableFeeItem> displayableFeeItemList;

  ApplicationPaymentDisplaySummary(String headlineSummary,
                                          String formattedAmount,
                                          List<DisplayableFeeItem> displayableFeeItemList) {
    this.headlineSummary = headlineSummary;
    this.formattedAmount = formattedAmount;
    this.displayableFeeItemList = displayableFeeItemList;
  }

  public String getHeadlineSummary() {
    return headlineSummary;
  }

  public String getFormattedAmount() {
    return formattedAmount;
  }

  public List<DisplayableFeeItem> getDisplayableFeeItemList() {
    return displayableFeeItemList;
  }
}
