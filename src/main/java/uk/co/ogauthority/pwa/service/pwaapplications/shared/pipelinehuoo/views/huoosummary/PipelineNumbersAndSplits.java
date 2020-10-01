package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryAndSplit;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;


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
                                              PadPipelineSummaryAndSplit padPipelineSummaryAndSplit) {
    return new PipelineNumbersAndSplits(
        pipelineIdentifier,
        padPipelineSummaryAndSplit.getPadPipelineSummaryDto().getPipelineNumber(),
        padPipelineSummaryAndSplit.getSplitInfo() // no split info for whole pipelines
    );
  }

}
