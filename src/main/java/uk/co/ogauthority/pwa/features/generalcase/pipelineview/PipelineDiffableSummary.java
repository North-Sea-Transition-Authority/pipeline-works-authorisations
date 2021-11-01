package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderConditionalQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;

/**
 * Designed to be consumed by the pipeline summary service and associated templates.
 */

public final class PipelineDiffableSummary {
  private final PipelineId pipelineId;
  private final PipelineHeaderView pipelineHeaderView;
  private final List<IdentDiffableView> identViews;
  private final PipelineDrawingSummaryView drawingSummaryView;

  private PipelineDiffableSummary(PipelineId pipelineId,
                                  PipelineHeaderView pipelineHeaderView,
                                  List<IdentDiffableView> identViews,
                                  PipelineDrawingSummaryView drawingSummaryView) {
    this.pipelineId = pipelineId;
    this.pipelineHeaderView = pipelineHeaderView;
    this.identViews = identViews;
    this.drawingSummaryView = drawingSummaryView;
  }

  public static PipelineDiffableSummary from(PipelineHeaderView pipelineHeaderView, List<IdentView> identViews,
                                             PipelineDrawingSummaryView drawingSummaryViews) {

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
        diffableIdents,
        drawingSummaryViews
    );

  }

  public static PipelineDiffableSummary empty() {
    return new PipelineDiffableSummary(null, new PipelineHeaderView(), List.of(), null);

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

  public PipelineDrawingSummaryView getDrawingSummaryView() {
    return drawingSummaryView;
  }

  public Set<PipelineHeaderConditionalQuestion> getQuestionsForPipelineStatus() {
    return pipelineHeaderView.getQuestionsForPipelineStatus();
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
        && Objects.equals(identViews, that.identViews)
        && Objects.equals(drawingSummaryView, that.drawingSummaryView);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineId, pipelineHeaderView, identViews, drawingSummaryView);
  }
}
