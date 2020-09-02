package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;

/**
 * Designed to be consumed by the pipeline summary service and associated templates.
 */
public final class PipelineDiffableSummary {
  private final PipelineId pipelineId;
  private final PipelineHeaderView pipelineHeaderView;
  private final List<IdentDiffableView> identViews;

  private PipelineDiffableSummary(PipelineId pipelineId,
                                  PipelineHeaderView pipelineHeaderView,
                                  List<IdentDiffableView> identViews) {
    this.pipelineId = pipelineId;
    this.pipelineHeaderView = pipelineHeaderView;
    this.identViews = identViews;
  }

  public static PipelineDiffableSummary from(PipelineHeaderView pipelineHeaderView, List<IdentView> identViews) {

    var diffableIdents = new ArrayList<IdentDiffableView>();

    for (int i = 0; i < identViews.size(); i++) {
      var currentIdent = identViews.get(i);
      var nextIdent = i != identViews.size() - 1 ? identViews.get(i + 1) : null;
      var previousIdent = i > 0 ? identViews.get(i - 1) : null;
      diffableIdents.add(
          IdentDiffableView.fromIdentViews(
              previousIdent,
              currentIdent,
              nextIdent
          ));

    }

    return new PipelineDiffableSummary(
        PipelineId.from(pipelineHeaderView),
        pipelineHeaderView,
        diffableIdents
    );

  }

  public static PipelineDiffableSummary empty() {
    return new PipelineDiffableSummary(null, new PipelineHeaderView(), List.of());
  }


  public PipelineId getPipelineId() {
    return pipelineId;
  }

  public PipelineHeaderView getPipelineHeaderView() {
    return pipelineHeaderView;
  }

  public List<IdentDiffableView> getIdentViews() {
    return identViews;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineDiffableSummary that = (PipelineDiffableSummary) o;
    return Objects.equals(pipelineId, that.pipelineId)
        && Objects.equals(pipelineHeaderView, that.pipelineHeaderView)
        && Objects.equals(identViews, that.identViews);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineId, pipelineHeaderView, identViews);
  }
}
