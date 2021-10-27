package uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSummaryAndSplit;

/**
 * <p>A single pipeline or pipeline section including all information needed to compute a displayable reference.</p>
 * <p>this class fits under the pipeline hupop domain as pipeline sections(splits) only exist within the context of a huoo role.</p>
 */
public class PipelineNumbersAndSplits {

  private final PipelineIdentifier pipelineIdentifier;
  private final String pipelineNumber;
  private final String splitInfo;


  public PipelineNumbersAndSplits(PipelineIdentifier pipelineIdentifier, String pipelineNumber,
                                  String splitInfo) {
    this.pipelineIdentifier = pipelineIdentifier;
    this.pipelineNumber = pipelineNumber;
    this.splitInfo = splitInfo;
  }

  public PipelineIdentifier getPipelineIdentifier() {
    return pipelineIdentifier;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public String getSplitInfo() {
    return splitInfo;
  }


  public static PipelineNumbersAndSplits from(PipelineIdentifier pipelineIdentifier,
                                              PipelineSummaryAndSplit pipelineSummaryAndSplit) {
    return new PipelineNumbersAndSplits(
        pipelineIdentifier,
        pipelineSummaryAndSplit.getPipelineOverview().getPipelineNumber(),
        pipelineSummaryAndSplit.getSplitInfo() // no split info for whole pipelines
    );
  }


  @Override
  public String toString() {
    var splitInfoFormatted = splitInfo != null ? "[" +  splitInfo + "]" : "";
    return pipelineNumber + " " +  splitInfoFormatted;
  }
}
