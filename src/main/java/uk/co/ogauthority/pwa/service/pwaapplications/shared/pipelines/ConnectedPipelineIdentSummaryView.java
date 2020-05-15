package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;

public class ConnectedPipelineIdentSummaryView {

  private final List<ConnectedPipelineIdentsView> connectedPipelineIdents;

  public ConnectedPipelineIdentSummaryView(
      List<ConnectedPipelineIdentsView> connectedPipelineIdents) {
    this.connectedPipelineIdents = connectedPipelineIdents;
  }

  public List<ConnectedPipelineIdentsView> getConnectedPipelineIdents() {
    return connectedPipelineIdents;
  }
}
