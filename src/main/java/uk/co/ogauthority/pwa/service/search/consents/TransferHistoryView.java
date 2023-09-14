package uk.co.ogauthority.pwa.service.search.consents;

public class TransferHistoryView {

  private Integer originalPipelineId;
  private String transfereeConsentReference;
  private String viewUrl;

  public Integer getOriginalPipelineId() {
    return originalPipelineId;
  }

  public TransferHistoryView setOriginalPipelineId(Integer originalPipelineId) {
    this.originalPipelineId = originalPipelineId;
    return this;
  }

  public String getTransfereeConsentReference() {
    return transfereeConsentReference;
  }

  public TransferHistoryView setTransfereeConsentReference(String transfereeConsentReference) {
    this.transfereeConsentReference = transfereeConsentReference;
    return this;
  }

  public String getViewUrl() {
    return viewUrl;
  }

  public TransferHistoryView setViewUrl(String viewUrl) {
    this.viewUrl = viewUrl;
    return this;
  }
}
