package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display;

public class DisplayableFeeItem {

  private final String description;
  private final String formattedAmount;

  DisplayableFeeItem(String description, String formattedAmount) {
    this.description = description;
    this.formattedAmount = formattedAmount;
  }

  public String getDescription() {
    return description;
  }

  public String getFormattedAmount() {
    return formattedAmount;
  }
}
