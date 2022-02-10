package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;

/**
 * General purpose class to hold all information about a pipeline.
 */
public class PipelineAndIdentView {

  private final PipelineId pipelineId;
  private final PipelineOverview pipelineOverview;
  private final List<IdentView> sortedIdentViews;

  public PipelineAndIdentView(PipelineOverview pipelineOverview,
                              List<IdentView> identViews) {
    this.pipelineId = PipelineId.from(pipelineOverview);
    this.pipelineOverview = pipelineOverview;
    this.sortedIdentViews = identViews.stream()
        .sorted(Comparator.comparing(IdentView::getIdentNumber))
        .collect(Collectors.toUnmodifiableList());
  }

  public PipelineId getPipelineId() {
    return pipelineId;
  }

  public PipelineOverview getPipelineOverview() {
    return pipelineOverview;
  }

  public List<IdentView> getSortedIdentViews() {
    return sortedIdentViews;
  }
}
