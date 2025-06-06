package uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineIdentifierSplitInfoVisitor;

public class PipelineSummaryAndSplit {

  private final PipelineOverview pipelineOverview;
  private final String splitInfo;


  public PipelineSummaryAndSplit(PipelineOverview pipelineOverview, String splitInfo) {
    this.pipelineOverview = pipelineOverview;
    this.splitInfo = splitInfo;
  }

  public static PipelineSummaryAndSplit duplicateOptionForPipelineIdentifier(PipelineIdentifier pipelineIdentifier,
                                                                             PipelineOverview pipelineOverview) {
    var splitInfoVisitor = new PipelineIdentifierSplitInfoVisitor();
    pipelineIdentifier.accept(splitInfoVisitor);

    return new PipelineSummaryAndSplit(
        pipelineOverview,
        splitInfoVisitor.getSplitInfoDetails()
    );
  }


  public PipelineOverview getPipelineOverview() {
    return pipelineOverview;
  }

  public String getSplitInfo() {
    return splitInfo;
  }

}
