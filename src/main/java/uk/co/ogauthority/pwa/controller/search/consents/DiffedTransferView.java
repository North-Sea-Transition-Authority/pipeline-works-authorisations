package uk.co.ogauthority.pwa.controller.search.consents;

public class DiffedTransferView {

  private final boolean transferLinksVisible;

  private final String transferFromUrl;

  private final String transferToUrl;

  public DiffedTransferView(boolean transferLinksVisible, String transferFromUrl, String transferToUrl) {
    this.transferLinksVisible = transferLinksVisible;
    this.transferFromUrl = transferFromUrl;
    this.transferToUrl = transferToUrl;
  }

  public boolean isTransferLinksVisible() {
    return transferLinksVisible;
  }

  public String getTransferFromUrl() {
    return transferFromUrl;
  }

  public String getTransferToUrl() {
    return transferToUrl;
  }
}
