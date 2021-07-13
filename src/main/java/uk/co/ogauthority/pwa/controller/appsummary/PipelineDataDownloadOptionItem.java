package uk.co.ogauthority.pwa.controller.appsummary;

public final class PipelineDataDownloadOptionItem {

  private final int displayOrder;
  private final String displayString;
  private final String downloadUrl;

  PipelineDataDownloadOptionItem(int displayOrder, String displayString, String downloadUrl) {
    this.displayOrder = displayOrder;
    this.displayString = displayString;
    this.downloadUrl = downloadUrl;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayString() {
    return displayString;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }
}
