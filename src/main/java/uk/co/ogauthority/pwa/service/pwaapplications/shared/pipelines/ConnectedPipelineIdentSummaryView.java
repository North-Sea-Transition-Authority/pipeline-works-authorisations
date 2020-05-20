package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;

/**
 * This view provides a collection of ConnectedPipelineIdentsViews.
 * These can be iterated to get all idents of a pipeline.
 * Each view containing a continuous connection of idents without a gap.
 */
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
