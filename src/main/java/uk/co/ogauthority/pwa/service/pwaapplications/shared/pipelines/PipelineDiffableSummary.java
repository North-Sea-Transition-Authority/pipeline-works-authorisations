package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipeline;

/**
 * Designed to be consumed by the pipeline summary service and associated templates.
 */
public class PipelineDiffableSummary {
  private final PipelineId pipelineId;
  private final String pipelineName;
  private final List<IdentDiffableView> identViews;

  private PipelineDiffableSummary(PipelineId pipelineId, String pipelineName,
                                  List<IdentDiffableView> identViews) {
    this.pipelineId = pipelineId;
    this.pipelineName = pipelineName;
    this.identViews = identViews;
  }

  public static PipelineDiffableSummary from(NamedPipeline namedPipeline, List<IdentView> identViews) {

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
        PipelineId.from(namedPipeline),
        namedPipeline.getPipelineName(),
        diffableIdents
    );

  }


  public PipelineId getPipelineId() {
    return pipelineId;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public List<IdentDiffableView> getIdentViews() {
    return identViews;
  }
}
