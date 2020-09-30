package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary;

import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineOption;


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


  public static PipelineNumbersAndSplits from(PadPipelineSummaryDto padPipelineSummaryDto) {
    return new PipelineNumbersAndSplits(
        null,
        padPipelineSummaryDto.getPipelineNumber(),
        null // no split info for whole pipelines
    );
  }

  public static PipelineNumbersAndSplits from(PipelineIdentifier pipelineIdentifier,
                                                                                PickableHuooPipelineOption pickableHuooPipelineOption) {
    return new PipelineNumbersAndSplits(
        pipelineIdentifier,
        pickableHuooPipelineOption.getPipelineNumber(),
        pickableHuooPipelineOption.getSplitInfo() // no split info for whole pipelines
    );
  }

}
