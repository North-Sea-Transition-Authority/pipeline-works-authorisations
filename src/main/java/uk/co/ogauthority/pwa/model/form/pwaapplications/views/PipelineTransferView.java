package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

public class PipelineTransferView {
  private final String transferredFromRef;

  private final String transferredToRef;

  private final String intelligentlyPigged;

  private final Boolean compatibleWithCo2;

  public PipelineTransferView() {
    transferredFromRef = null;
    transferredToRef = null;
    intelligentlyPigged = null;
    compatibleWithCo2 = null;
  }

  public PipelineTransferView(String transferredFromRef, String transferredToRef, String intelligentlyPigged,
                              Boolean compatibleWithCo2) {
    this.transferredFromRef = transferredFromRef;
    this.transferredToRef = transferredToRef;
    this.intelligentlyPigged = intelligentlyPigged;
    this.compatibleWithCo2 = compatibleWithCo2;
  }

  public String getTransferredFromRef() {
    return transferredFromRef;
  }

  public String getTransferredToRef() {
    return transferredToRef;
  }

  public String getIntelligentlyPigged() {
    return intelligentlyPigged;
  }

  public Boolean getCompatibleWithCo2() {
    return compatibleWithCo2;
  }
}
