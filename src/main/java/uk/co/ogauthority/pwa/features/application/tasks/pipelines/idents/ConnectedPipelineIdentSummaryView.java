package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import java.util.List;

/**
 * This view provides a collection of ConnectedPipelineIdentsViews.
 * These can be iterated to get all idents of a pipeline.
 * Each view containing a continuous connection of idents without a gap.
 */
public class ConnectedPipelineIdentSummaryView {

  private final List<ConnectedPipelineIdentsView> connectedPipelineIdents;
  private final String totalIdentLength;

  public ConnectedPipelineIdentSummaryView(
      List<ConnectedPipelineIdentsView> connectedPipelineIdents, String totalIdentLength) {
    this.connectedPipelineIdents = connectedPipelineIdents;
    this.totalIdentLength = totalIdentLength;
  }

  public List<ConnectedPipelineIdentsView> getConnectedPipelineIdents() {
    return connectedPipelineIdents;
  }

  public String getTotalIdentLength() {
    return totalIdentLength;
  }
}
